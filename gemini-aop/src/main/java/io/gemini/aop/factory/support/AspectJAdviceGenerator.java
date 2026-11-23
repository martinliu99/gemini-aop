/*
 * Copyright © 2023 - present, the original author or authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gemini.aop.factory.support;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.gemini.aop.AdviceKind.AspectJAdviceKind;
import io.gemini.aop.factory.support.AdviceSpec.AspectJAdviceSpec;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.TypeResolutionStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

/**
 * Bytecode generator that produces a concrete {@link io.gemini.api.aop.Advice} class
 * wrapping an AspectJ advice method.
 * <p>
 * The generated class holds a reference to the AspectJ aspect instance and implements
 * the appropriate {@link io.gemini.api.aop.Advice.Before}, {@link io.gemini.api.aop.Advice.After},
 * or {@link io.gemini.api.aop.Advice.Around} interface. The advice method body is generated
 * via ASM to extract pointcut-bound parameters from the {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint}
 * and forward them to the original AspectJ method.
 * </p>
 *
 * @author   martin.liu
 */
enum AspectJAdviceGenerator {

    INSTANCE
    ;

    private static final TypeDescription JOINPOINT_TYPE = TypeDescription.ForLoadedType.of(Joinpoint.class);
    private static final TypeDescription MUTABLE_JOINPOINT_TYPE = TypeDescription.ForLoadedType.of(Joinpoint.MutableJoinpoint.class);

    private static final TypeDescription AROUND_ADVICE_TYPE = TypeDescription.ForLoadedType.of(Advice.Around.class);
    private static final String AROUND_ADVICE_METHOD_NAME = AROUND_ADVICE_TYPE.getSimpleName().toLowerCase(Locale.ENGLISH);

    private static final TypeDescription BEFORE_ADVICE_TYPE = TypeDescription.ForLoadedType.of(Advice.Before.class);
    private static final String BEFORE_ADVICE_METHOD_NAME = BEFORE_ADVICE_TYPE.getSimpleName().toLowerCase(Locale.ENGLISH);

    private static final TypeDescription AFTER_ADVICE_TYPE = TypeDescription.ForLoadedType.of(Advice.After.class);
    private static final String AFTER_ADVICE_METHOD_NAME = AFTER_ADVICE_TYPE.getSimpleName().toLowerCase(Locale.ENGLISH);


    @SuppressWarnings("unchecked")
    public DynamicType.Unloaded<? extends Advice> make(AspectJAdviceSpec.Loaded adviceSpec, boolean autoComputeAsm) {
        AspectJAdviceKind adviceKind = adviceSpec.getAdviceKind();
        TypeDescription adviceType = adviceSpec.getDeclaringType();

        // 1.define class with parent class & interfaces
        Generic superType = TypeDescription.Generic.Builder
                .parameterizedType(AspectJAdviceAdapter.ASPECTJ_ADPATER_TYPE, adviceType)
                .build();

        List<TypeDefinition> implementTypeDefinitions = new ArrayList<>(2);
        if (adviceKind.isAround()) {
            implementTypeDefinitions.add(AROUND_ADVICE_TYPE);
        } else if (adviceKind.isBefore()) {
            implementTypeDefinitions.add(BEFORE_ADVICE_TYPE);
        } else {
            implementTypeDefinitions.add(AFTER_ADVICE_TYPE);
        }

        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(superType, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .name(adviceSpec.getAdviceClassName())
                .modifiers(Modifier.PUBLIC | Opcodes.ACC_SYNTHETIC)
                .implement(implementTypeDefinitions)
                .visit( new AsmVisitorWrapper.ForDeclaredMethods()
                        .readerFlags(ClassReader.EXPAND_FRAMES)
                        .writerFlags(autoComputeAsm ? ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_MAXS)
                )
                ;


        // 2.define constructor
        builder = builder
                .defineConstructor(Modifier.PUBLIC)
                .withParameter(AspectJAdviceSpec.Loaded.class, AspectJAdviceAdapter.CONSTRUCTOR_PARAM0_ADVICE_SPEC)
                .withParameter(adviceType, AspectJAdviceAdapter.CONSTRUCTOR_PARAM1_DELEGATE_OBJECT)
                .intercept(
                        MethodCall.invoke(AspectJAdviceAdapter.CONSTRUCTOR).withAllArguments()
                );


        // 3.define advice method
        String methodName = adviceKind.isAround() 
                ? AROUND_ADVICE_METHOD_NAME 
                : (adviceKind.isBefore() ? BEFORE_ADVICE_METHOD_NAME : AFTER_ADVICE_METHOD_NAME);
        builder = builder
                .defineMethod(methodName, void.class, Modifier.PUBLIC)
                .withParameter(MUTABLE_JOINPOINT_TYPE, "joinpoint")
                .throwing(Throwable.class)
                .intercept( 
                        new AspectJAdviceMethodImplementation(adviceSpec) 
                )
                ;

        return (DynamicType.Unloaded<? extends Advice>) builder.make(TypeResolutionStrategy.Lazy.INSTANCE);
    }


    /**
     * ByteBuddy {@link Implementation} and {@link ByteCodeAppender} that generates the body
     * of the advice method in the dynamically created advice class.
     * Extracts pointcut-bound parameters from the joinpoint and invokes the original AspectJ method.
     */
    static class AspectJAdviceMethodImplementation implements Implementation, ByteCodeAppender {

        private static final Generic OBJECT = TypeDescription.ForLoadedType.of(Object.class).asGenericType();

        private static final int PARAM_INDEX0_THIS_OBJECT = 0;
        private static final int PARAM_INDEX1_MUTABLE_JOINPOINT = 1;

        private static final MethodDescription.InDefinedShape GET_ARGUMENTS_METHOD = JOINPOINT_TYPE.getDeclaredMethods().filter(named("getArguments")).getOnly();
        private static final MethodDescription.InDefinedShape GET_STATIC_PART_METHOD = JOINPOINT_TYPE.getDeclaredMethods().filter(named("getStaticPart")).getOnly();
        private static final MethodDescription.InDefinedShape GET_TARGET_OBJECT_METHOD = JOINPOINT_TYPE.getDeclaredMethods().filter(named("getTargetObject")).getOnly();

        private static final MethodDescription.InDefinedShape GET_RETURN_VALUE_METHOD = MUTABLE_JOINPOINT_TYPE.getDeclaredMethods().filter(named("getReturnValue")).getOnly();
        private static final MethodDescription.InDefinedShape GET_THROWN_EXCEPTION_METHOD = MUTABLE_JOINPOINT_TYPE.getDeclaredMethods().filter(named("getThrownException")).getOnly();


        private final AspectJAdviceSpec.Loaded adviceSpec;


        /**
         * Create AspectJAdviceMethodImplementation with given AspectJAdviceSpec
         * @param adviceSpec AspectJAdviceSpec
         */
        public AspectJAdviceMethodImplementation(AspectJAdviceSpec.Loaded adviceSpec) {
            this.adviceSpec = adviceSpec;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ByteCodeAppender appender(Target implementationTarget) {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Size apply(MethodVisitor methodVisitor, Context implementationContext, 
                MethodDescription instrumentedMethod) {
            int localVariableSize = instrumentedMethod.getStackSize();
            int operandsStackSize = 0;

            // 1.decide AfterReturning or AfterThrowing Advice invocation
            final Label nullCheck = new Label();
            AspectJAdviceKind adviceKind = adviceSpec.getAdviceKind();
            if (adviceKind.isAfterReturning() || adviceKind.isAfterThrowing()) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, MUTABLE_JOINPOINT_TYPE.getInternalName(), 
                        GET_THROWN_EXCEPTION_METHOD.getInternalName(), GET_THROWN_EXCEPTION_METHOD.getDescriptor(), true);

                // if (throwing == null) invoke AfterReturning Advice
                if (adviceKind.isAfterReturning())
                    methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, nullCheck);
                // if (throwing != null) invoke AfterThrowing Advice
                if (adviceKind.isAfterThrowing())
                    methodVisitor.visitJumpInsn(Opcodes.IFNULL, nullCheck);
            }


            // 2.invoke AspectJAdvice method
            // invoke delegatee.method(joinpoint, ...)

            // push target field for non-static method
            MethodDescription adviceMethod = adviceSpec.getAdviceMethod();
            if (!adviceMethod.isStatic()) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX0_THIS_OBJECT);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, AspectJAdviceAdapter.ASPECTJ_ADPATER_TYPE.getInternalName(),
                        AspectJAdviceAdapter.GET_DELEGATE_OBJECT_METHOD.getInternalName(), AspectJAdviceAdapter.GET_DELEGATE_OBJECT_METHOD.getDescriptor(), false);
                Assigner.DEFAULT.assign(OBJECT, adviceSpec.getDeclaringType().asGenericType(), Typing.DYNAMIC).apply(methodVisitor, implementationContext);

                operandsStackSize++;
            }

            // push arguments based on binder type
            List<ResolvedParameter> resolvedParameters = adviceSpec.getResolvedParameters();
            int paramIndex = 0;
            for (ResolvedParameter resolvedParameter : resolvedParameters) {
                Generic matchedType = resolvedParameter.getMatchedType();
                Generic parameterType = resolvedParameter.getParameterType();

                paramIndex++;
                operandsStackSize++;
                switch (resolvedParameter.getParameterCategory()) {
                    case JOINPOINT_PARAM:
                    case MUTABLE_JOINPOINT_PARAM:
                    case PROCEDDING_JOINPOINT_PARAM: {
                        // first parameter is Joinpoint
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);

                        break;
                    }
                    case STATIC_PART_PARAM: {
                        // first parameter is StaticPart
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, JOINPOINT_TYPE.getInternalName(), 
                                GET_STATIC_PART_METHOD.getInternalName(), GET_STATIC_PART_METHOD.getDescriptor(), true);

                        break;
                    }
                    case RETURNING_ANNOTATION: {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, MUTABLE_JOINPOINT_TYPE.getInternalName(), 
                                GET_RETURN_VALUE_METHOD.getInternalName(), GET_RETURN_VALUE_METHOD.getDescriptor(), true);

                        //
                        if (parameterType.equals(matchedType) || !parameterType.isPrimitive())
                            Assigner.DEFAULT.assign(OBJECT, parameterType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                        else {
                            Assigner.DEFAULT.assign(OBJECT, matchedType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                            Assigner.DEFAULT.assign(matchedType, parameterType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                        }

                        if (matchedType.getStackSize().getSize() > 1)
                            operandsStackSize++;

                        break;
                    }
                    case THROWING_ANNOTATION: {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, MUTABLE_JOINPOINT_TYPE.getInternalName(), 
                                GET_THROWN_EXCEPTION_METHOD.getInternalName(), GET_THROWN_EXCEPTION_METHOD.getDescriptor(), true);
                        Assigner.DEFAULT.assign(OBJECT, matchedType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);

                        break;
                    }
                    case THIS_VAR:
                    case TARGET_VAR: {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, MUTABLE_JOINPOINT_TYPE.getInternalName(), 
                                GET_TARGET_OBJECT_METHOD.getInternalName(), GET_TARGET_OBJECT_METHOD.getDescriptor(), true);
                        Assigner.DEFAULT.assign(OBJECT, matchedType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);

                        break;
                    }
                    case ARGS_VAR: {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, PARAM_INDEX1_MUTABLE_JOINPOINT);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, JOINPOINT_TYPE.getInternalName(), 
                                GET_ARGUMENTS_METHOD.getInternalName(), GET_ARGUMENTS_METHOD.getDescriptor(), true);

                        IntegerConstant.forValue(resolvedParameter.getArgsIndex()).apply(methodVisitor, implementationContext);
                        methodVisitor.visitInsn(Opcodes.AALOAD);

                        if (parameterType.equals(matchedType) || !parameterType.isPrimitive())
                            Assigner.DEFAULT.assign(OBJECT, parameterType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                        else {
                            Assigner.DEFAULT.assign(OBJECT, matchedType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                            Assigner.DEFAULT.assign(matchedType, parameterType, Typing.DYNAMIC).apply(methodVisitor, implementationContext);
                        }

                        operandsStackSize++;
                        if (matchedType.getStackSize().getSize() == 1 && paramIndex < resolvedParameters.size())
                            operandsStackSize--;
                        break;
                    }
                    default:
                        break;
                }
            }

            // invoke delegatee.method(joinpoint, ...)
            MethodInvocation.invoke(adviceMethod).apply(methodVisitor, implementationContext);

            // pop the return value if exists
            Generic returnType = adviceMethod.getReturnType();
            int stackSize = returnType.getStackSize().getSize();
            if (stackSize > 0) {
                methodVisitor.visitInsn(stackSize == 2 ? Opcodes.POP2 : Opcodes.POP);
            }


            // 3.mark method return label
            if (adviceKind.isAfterReturning() || adviceKind.isAfterThrowing()) {
                methodVisitor.visitLabel(nullCheck);
                methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);     // calculate stack frame map
            }


            // 4.return
            methodVisitor.visitInsn(Opcodes.RETURN);

            return new Size(operandsStackSize, localVariableSize);      // adjust local variable table size
        }
    }
}