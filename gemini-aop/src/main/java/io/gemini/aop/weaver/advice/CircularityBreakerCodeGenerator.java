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
package io.gemini.aop.weaver.advice;

import static net.bytebuddy.jar.asm.Opcodes.ALOAD;
import static net.bytebuddy.jar.asm.Opcodes.ASM9;
import static net.bytebuddy.jar.asm.Opcodes.ASTORE;
import static net.bytebuddy.jar.asm.Opcodes.ATHROW;
import static net.bytebuddy.jar.asm.Opcodes.DCONST_1;
import static net.bytebuddy.jar.asm.Opcodes.FCONST_1;
import static net.bytebuddy.jar.asm.Opcodes.ICONST_1;
import static net.bytebuddy.jar.asm.Opcodes.IFNE;
import static net.bytebuddy.jar.asm.Opcodes.INVOKESTATIC;
import static net.bytebuddy.jar.asm.Opcodes.IRETURN;
import static net.bytebuddy.jar.asm.Opcodes.LCONST_1;
import static net.bytebuddy.jar.asm.Opcodes.RETURN;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.gemini.aop.weaver.AopWeaver;
import io.gemini.aop.weaver.BootstrapDispatcher;
import io.gemini.api.aop.annotation.EnableCircularityBreaker;
import io.gemini.core.bootstrap.BootstrapClassConsumer;
import io.gemini.core.loading.ClassLoadingStrategySelector;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.TypeResolutionStrategy;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.StubMethod;
import net.bytebuddy.implementation.attribute.MethodAttributeAppender;
import net.bytebuddy.implementation.bytecode.constant.DefaultValue;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaConstant;

/**
 * Generates bytecode that wraps ByteBudyy framework advice methods with circularity-breaking guards.
 * <p>
 * Two wrapping strategies are provided:
 * <ul>
 *   <li>{@link #wrapMethodImplementation(Class)} – rewrites the advice method body to check
 *       {@link BootstrapDispatcher#isDispatchable()} before executing, preventing re-entrant
 *       dispatch when the advice itself triggers the same instrumented joinpoint.</li>
 *   <li>{@link #bridgeMethodCall(Class, TypeDescription, ClassFileLocator)} – replaces the advice method body with an INDY call
 *       that checks dispatchability before delegating to the original advice logic.</li>
 *   <li>{@link #postProcessTargetMethod(int)} – patches the callback slot constant in the
 *       target method's INDY instruction after the advice class has been generated.</li>
 * </ul>
 * </p>
 *
 * @author   martin.liu
 */
@BootstrapClassConsumer
public enum CircularityBreakerCodeGenerator {

    INSTANCE;


    private static final String CIRCULARITY_BREAKER_CLASSNAME = "$CircularityBreaker";

    private static final TypeDescription BOOTSTRAP_DISPATCHER_TYPE;
    private static final MethodDescription IS_DISPATCHABLE_METHOD;
    private static final MethodDescription DISABLE_DISPATCH_METHOD;
    private static final MethodDescription ENABLE_DISPATCH_METHOD;

    static {
        BOOTSTRAP_DISPATCHER_TYPE = TypeDescription.ForLoadedType.of(BootstrapDispatcher.class);

        IS_DISPATCHABLE_METHOD = BOOTSTRAP_DISPATCHER_TYPE.getDeclaredMethods()
                .filter( 
                        isStatic()
                        .and( isPublic() )
                        .and( named("isDispatchable") )
                        .and( returns(boolean.class) )
                )
                .getOnly();
        DISABLE_DISPATCH_METHOD = BOOTSTRAP_DISPATCHER_TYPE.getDeclaredMethods()
                .filter( 
                        isStatic()
                        .and( isPublic() )
                        .and( named("disableDispatch") )
                        .and( returns( void.class) )
                )
                .getOnly();
        ENABLE_DISPATCH_METHOD = BOOTSTRAP_DISPATCHER_TYPE.getDeclaredMethods()
                .filter( 
                        isStatic()
                        .and( isPublic() )
                        .and( named("enableDispatch") )
                        .and( returns( void.class) )
                )
                .getOnly();
    }


    /**
     * Wraps the advice method implementation with a dispatchability guard.
     * The generated class checks {@link BootstrapDispatcher#isDispatchable()} at entry
     * and disables/re-enables dispatch around the original method body.
     *
     * @param adviceClass the original framework advice class to wrap
     * @return a loaded dynamic type containing the wrapped advice class
     */
    public DynamicType.Loaded<?> wrapMethodImplementation(Class<?> adviceClass) {
        return new ByteBuddy()
                .redefine(adviceClass)
                .name(adviceClass.getName() + CIRCULARITY_BREAKER_CLASSNAME)
                .visit(
                        new AsmVisitorWrapper.ForDeclaredMethods()
                        .readerFlags(ClassReader.EXPAND_FRAMES)
                        .writerFlags(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                        .invokable(
                                adviceMethodMatcher(adviceClass), 
                                MethodImplementationWrapper.INSTANCE
                        ) 
                )
                .make(TypeResolutionStrategy.Lazy.INSTANCE)
                .load(adviceClass.getClassLoader(), ClassLoadingStrategySelector.Default.SINGLETON.select(adviceClass));
    }

    private static ElementMatcher<MethodDescription> adviceMethodMatcher(Class<?> adviceClass) {
        List<ElementMatcher<? super MethodDescription>> adviceMethodMatchers = new ArrayList<>(2);
        for (Method method : adviceClass.getDeclaredMethods()) {
            if (method.getDeclaredAnnotation(OnMethodEnter.class) == null
                    && method.getDeclaredAnnotation(OnMethodExit.class) == null)
                continue;

            adviceMethodMatchers.add(
                    named( method.getName() )
                    .and( takesArguments( method.getParameterTypes() ) )
                    .and( returns( method.getReturnType() ) )
            );
        }
        return new ElementMatcher.Junction.Disjunction<MethodDescription>(adviceMethodMatchers);
    }


    /**
     * Wraps the advice method call with a dispatchability guard using an INDY-based delegation.
     * The generated class replaces the method body with an INDY call that checks
     * {@link BootstrapDispatcher#isDispatchable()} before invoking the original advice logic.
     * 
     * @param adviceClass the original ByteBuddy advice class to wrap
     * @param adviceType the ByeBuddy type description of adviceClass
     * @param classFileLocator the class file locator to locate binary reprensention of adviceType
     * @return a loaded dynamic type containing the wrapped advice class
     */
    public DynamicType.Loaded<?> bridgeMethodCall(Class<?> adviceClass, TypeDescription adviceType, 
            ClassFileLocator classFileLocator) {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .name(adviceType.getTypeName() + CIRCULARITY_BREAKER_CLASSNAME)
                ;

        for (MethodDescription adviceMethod : this.getAdviceMethod(adviceType)) {
            boolean breakCircularity = adviceMethod.getDeclaredAnnotations().isAnnotationPresent(EnableCircularityBreaker.class);
            builder = builder
                    // copy method signature
                    .define(adviceMethod)
                    .intercept(StubMethod.INSTANCE)
                    .attribute( MethodAttributeAppender.Explicit.of(adviceMethod) ) 
                    // redefine method body
                    .visit(
                            new AsmVisitorWrapper.ForDeclaredMethods()
                            .readerFlags(ClassReader.EXPAND_FRAMES)
                            .writerFlags(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                            .invokable(
                                    named( adviceMethod.getName() )
                                    .and( takesArguments( adviceMethod.getParameters().asTypeList().asErasures() ) )
                                    .and( returns( adviceMethod.getReturnType().asErasure() ) ),
                                breakCircularity ? MethodCallBridger.CIRCULARITY_DETECTED : MethodCallBridger.DIRECT_INDY_CALL
                            )
                    );
        }

        return builder.make(TypeResolutionStrategy.Lazy.INSTANCE)
                .load(adviceClass.getClassLoader(), ClassLoadingStrategySelector.Default.SINGLETON.select(adviceClass))
                ;
    }

    private List<MethodDescription> getAdviceMethod(TypeDescription adviceType) {
        List<MethodDescription> adviceMethods = new ArrayList<>(2);
        for (MethodDescription method : adviceType.getDeclaredMethods()) {
            AnnotationList methodAnnotations = method.getDeclaredAnnotations();
            if (!methodAnnotations.isAnnotationPresent(OnMethodEnter.class)
                    && !methodAnnotations.isAnnotationPresent(OnMethodExit.class) )
                continue;

            adviceMethods.add(method);
        }
        return adviceMethods;
    }



    /**
     * Returns a {@link MethodVisitorWrapper} that patches the callback slot constant
     * in the target method's INDY instruction to the given {@code callbackSlot} value.
     * Called after the advice class has been generated to bind the correct slot index.
     *
     * @param callbackSlot the callback slot index assigned to this advisor weaver
     * @return a method visitor wrapper that patches the INDY bootstrap argument
     */
    public MethodVisitorWrapper postProcessTargetMethod(int callbackSlot) {
        return new TargetMethodWrapper(callbackSlot);
    }


    /**
     * {@link MethodVisitorWrapper} that rewrites the advice method body to check
     * {@link io.gemini.aop.weaver.BootstrapDispatcher#isDispatchable()} and
     * disable/re-enable dispatch around the original method body.
     */
    static enum MethodImplementationWrapper implements MethodVisitorWrapper {

        INSTANCE;


        /**
         *  {@inheritDoc}
         */
        @Override
        public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
                MethodVisitor methodVisitor, Context implementationContext, TypePool typePool, int writerFlags,
                int readerFlags) {

            return new MethodImplementationVisitor(methodVisitor, instrumentedMethod);
        }


        /**
         * ASM {@link MethodVisitor} that inserts dispatchability guard code at the
         * beginning of the advice method and re-enables dispatch before each return.
         */
        static class MethodImplementationVisitor extends MethodVisitor {

            private final MethodDescription instrumentedMethod;

            private final Label methodEnter = new Label();
            private final Label methodExit = new Label();

            private final List<LocalVariableHolder> localVariables = new ArrayList<>();


            public MethodImplementationVisitor(MethodVisitor methodVisitor, MethodDescription instrumentedMethod) {
                super(ASM9, methodVisitor);

                this.instrumentedMethod = instrumentedMethod;
            }


            /**
             *  {@inheritDoc}
             */
            @Override
            public void visitCode() {
                // enter origin method body
                super.visitCode();

                MethodVisitor methodVisitor = this.getDelegate();

                Label ifTrue = new Label();

                visitLabel(methodEnter);

                // 1.check whether is dispatchable before disabling dispatch
                visitMethodInsn(
                        INVOKESTATIC,
                        BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                        IS_DISPATCHABLE_METHOD.getName(),
                        IS_DISPATCHABLE_METHOD.getDescriptor(),
                        false
                );

                visitJumpInsn(IFNE, ifTrue);

                DefaultValue.of(instrumentedMethod.getReturnType()).apply(methodVisitor, null);
                MethodReturn.of(instrumentedMethod.getReturnType()).apply(methodVisitor, null);


                visitLabel(ifTrue);

                visitMethodInsn(
                        INVOKESTATIC,
                        BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                        DISABLE_DISPATCH_METHOD.getName(),
                        DISABLE_DISPATCH_METHOD.getDescriptor(),
                        false
                );
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void visitInsn(int opcode) {
                if (opcode >= IRETURN && opcode <= RETURN) {
                    visitMethodInsn(
                            INVOKESTATIC,
                            BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                            ENABLE_DISPATCH_METHOD.getName(),
                            ENABLE_DISPATCH_METHOD.getDescriptor(),
                            false
                    );
                }

                super.visitInsn(opcode);
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, 
                    Label start, Label end,
                    int index) {
                localVariables.add(
                        new LocalVariableHolder(name, descriptor, signature, index) );
            }

            /**
             *  {@inheritDoc}
             */
            @Override
            public void visitEnd() {
                visitLabel(methodExit);

                // adjust local variable table.
                for (LocalVariableHolder localVariable : localVariables)
                    super.visitLocalVariable(
                            localVariable.name, 
                            localVariable.descriptor, localVariable.signature, 
                            methodEnter, methodExit,
                            localVariable.index
                    );

                super.visitEnd();
            }
        }


        /**
         * Holds local variable metadata for re-emission in {@code visitEnd}.
         */
        static class LocalVariableHolder {

            public final String name;
            public final String descriptor;
            public final String signature;
            public final int index;


            public LocalVariableHolder(String name, String descriptor, String signature, int index) {
                super();
                this.name = name;
                this.descriptor = descriptor;
                this.signature = signature;
                this.index = index;
            }
        }
    }


    /**
     * {@link MethodVisitorWrapper} that replaces the advice method body with an INDY call
     * that checks dispatchability before delegating to the original advice logic.
     */
    static enum MethodCallBridger implements MethodVisitorWrapper {

        DIRECT_INDY_CALL {
            /**
             *  {@inheritDoc}
             */
            @Override
            public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
                    MethodVisitor methodVisitor, Context implementationContext, TypePool typePool, int writerFlags,
                    int readerFlags) {

                return new DirectINDYCallVisitor(methodVisitor, instrumentedMethod);
            }
        },

        CIRCULARITY_DETECTED  {
            /**
             *  {@inheritDoc}
             */
            @Override
            public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
                    MethodVisitor methodVisitor, Context implementationContext, TypePool typePool, int writerFlags,
                    int readerFlags) {

                return new CircularityDetectedINDYCallVisitor(methodVisitor, instrumentedMethod);
            }
        };


        /**
         * {@inheritDoc}
         */
        @Override
        public abstract MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
                MethodVisitor methodVisitor, Context implementationContext, TypePool typePool, int writerFlags,
                int readerFlags);


        static abstract class AbstractBase extends MethodVisitor {

            private static final String ON_METHOD_ENTER_DESCRIPTOR = TypeDescription.ForLoadedType.of(OnMethodEnter.class).getDescriptor();
            private static final String ON_METHOD_EXIT_DESCRIPTOR = TypeDescription.ForLoadedType.of(OnMethodExit.class).getDescriptor();

            private final MethodDescription instrumentedMethod;


            protected AbstractBase(MethodVisitor methodVisitor,  MethodDescription instrumentedMethod) {
                super(ASM9, methodVisitor);

                this.instrumentedMethod = instrumentedMethod;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationVisitor annotationVisitor =  super.visitAnnotation(descriptor, visible);
                if (descriptor.equals(ON_METHOD_ENTER_DESCRIPTOR) 
                        || descriptor.equals(ON_METHOD_EXIT_DESCRIPTOR))
                    return new AnnotationAttributeModifier(annotationVisitor);

                return annotationVisitor;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void visitCode() {
                MethodVisitor methodVisitor = this.mv;

                // ignore existing method body
                this.mv = null; 

                // outut byte code
                methodVisitor.visitCode();

                doAppend(methodVisitor, instrumentedMethod);

                methodVisitor.visitEnd();
            }

            protected abstract void doAppend(MethodVisitor methodVisitor, MethodDescription instrumentedMethod);

            protected void invokeAdviceMethod(MethodVisitor methodVisitor, MethodDescription instrumentedMethod,
                    Generic returnType) {
                MethodVariableAccess.allArgumentsOf(instrumentedMethod).apply(methodVisitor, null);

                MethodInvocation.invoke(AopWeaver.BOOTSTRAP_DISPATCHER_CALLBACK_METHOD).dynamic(
                        instrumentedMethod.getName(), 
                        returnType.asErasure(), 
                        instrumentedMethod.getParameters().asTypeList().asErasures(), 
                        Arrays.asList(
                                JavaConstant.Simple.wrap(TargetMethodWrapper.CALLBACK_SLOT_PLACEHOLDER),
                                JavaConstant.Simple.wrap(instrumentedMethod.getDeclaringType().getTypeName().replace(CIRCULARITY_BREAKER_CLASSNAME, ""))

                        )
                )
                .apply(methodVisitor, null);
            }


            /**
             * ASM {@link AnnotationVisitor} that forces {@code inline = true} on
             * {@code @OnMethodEnter} and {@code @OnMethodExit} annotations.
             */
            static class AnnotationAttributeModifier extends AnnotationVisitor {

                public AnnotationAttributeModifier(AnnotationVisitor annotationVisitor) {
                    super(ASM9, annotationVisitor);
                }


                /**
                 * {@inheritDoc}
                 */
                @Override
                public void visit(String name, Object value) {
                    super.visit(name, name.equals("inline") ? true : value);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                    return new AnnotationAttributeModifier(
                            super.visitAnnotation(name, descriptor) );
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationVisitor visitArray(String name) {
                    return new AnnotationAttributeModifier(
                            super.visitArray(name) );
                }
            }
        }


        static class DirectINDYCallVisitor extends AbstractBase {

            public DirectINDYCallVisitor(MethodVisitor methodVisitor, MethodDescription instrumentedMethod) {
                super(methodVisitor, instrumentedMethod);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void doAppend(MethodVisitor methodVisitor, MethodDescription instrumentedMethod) {
                Generic returnType = instrumentedMethod.getReturnType();

                invokeAdviceMethod(methodVisitor, instrumentedMethod, returnType);
                MethodReturn.of(returnType).apply(methodVisitor, null);

                // compute stack and local size
                methodVisitor.visitMaxs(0, 0);
            }
        }


        /**
         * ASM {@link MethodVisitor} that replaces the method body with a dispatchability
         * check followed by an INDY call to the original advice logic.
         */
        static class CircularityDetectedINDYCallVisitor extends AbstractBase {

            public CircularityDetectedINDYCallVisitor(MethodVisitor methodVisitor, MethodDescription instrumentedMethod) {
                super(methodVisitor, instrumentedMethod);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            protected void doAppend(MethodVisitor methodVisitor, MethodDescription instrumentedMethod) {
                Generic returnType = instrumentedMethod.getReturnType();
                boolean hasReturn = !returnType.represents(void.class);

                int localVariableSize = instrumentedMethod.getStackSize();

                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label handlerStart = new Label();


                // 1.check whether is dispatchable before entering try block
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                        IS_DISPATCHABLE_METHOD.getName(),
                        IS_DISPATCHABLE_METHOD.getDescriptor(),
                        false
                );

                methodVisitor.visitJumpInsn(IFNE, tryStart);

                // return 'default value'
                returnDefaultValue(methodVisitor, instrumentedMethod, returnType, hasReturn);


                // 2.enter try block
                methodVisitor.visitTryCatchBlock(tryStart, tryEnd, handlerStart, null);

                methodVisitor.visitLabel(tryStart);

                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                        DISABLE_DISPATCH_METHOD.getName(),
                        DISABLE_DISPATCH_METHOD.getDescriptor(),
                        false
                );


                // invoke advice method via INDY
                invokeAdviceMethod(methodVisitor, instrumentedMethod, returnType);

                int returnValueVar = 0;
                if (hasReturn) {
                    returnValueVar = localVariableSize++;
                    MethodVariableAccess.of(returnType).storeAt(returnValueVar);
                }

                invokeFinallyBlock(methodVisitor);

                // return 
                if (hasReturn)
                    MethodVariableAccess.of(returnType).loadFrom(returnValueVar);
                MethodReturn.of(returnType).apply(methodVisitor, null);

                // exit try block
                methodVisitor.visitLabel(tryEnd);


                // 3.enter exception handler block
                methodVisitor.visitLabel(handlerStart);

                int exceptionVar = localVariableSize++;
                methodVisitor.visitVarInsn(ASTORE, exceptionVar);

                invokeFinallyBlock(methodVisitor);

                methodVisitor.visitVarInsn(ALOAD, exceptionVar);
                methodVisitor.visitInsn(ATHROW);


                // 4.compute stack and local size
                methodVisitor.visitMaxs(0, localVariableSize);
            }

            private void returnDefaultValue(MethodVisitor methodVisitor, MethodDescription instrumentedMethod,
                    Generic returnType, boolean hasReturn) {
                if (!hasReturn)
                    return;

                AnnotationDescription.Loadable<OnMethodEnter> onMethodEnterAnnotation = instrumentedMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class);
                TypeDescription skipOnType = onMethodEnterAnnotation != null 
                        ? (TypeDescription) onMethodEnterAnnotation.getValue("skipOn").resolve()
                        : TypeDescription.ForLoadedType.of(void.class);

                // to execute target method
                // 1) for Advice.OnDefaultValue return non-default value,
                if (skipOnType.represents(Advice.OnDefaultValue.class)) {
                    // push non-default value
                    if (!returnType.isPrimitive()) {
                        // unsupported scenario: could not create type instance.
                        ;
                    } else if (returnType.represents(long.class))
                        methodVisitor.visitInsn(LCONST_1);
                    else if (returnType.represents(double.class))
                        methodVisitor.visitInsn(DCONST_1);
                    else if (returnType.represents(float.class))
                        methodVisitor.visitInsn(FCONST_1);
                    else
                        methodVisitor.visitInsn(ICONST_1);
                } else {
                    // 2) for Advice.OnNonDefaultValue, and others, return default value,
                    DefaultValue.of(returnType).apply(methodVisitor, null);
                }

                MethodReturn.of(returnType).apply(methodVisitor, null);
            }

            private void invokeFinallyBlock(MethodVisitor methodVisitor) {
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        BOOTSTRAP_DISPATCHER_TYPE.getInternalName(), 
                        ENABLE_DISPATCH_METHOD.getName(),
                        ENABLE_DISPATCH_METHOD.getDescriptor(),
                        false
                );
            }
        }
    }


    /**
     * {@link MethodVisitorWrapper} that patches the callback slot constant in the
     * target method's INDY instruction after the advice class has been generated.
     */
    static class TargetMethodWrapper implements MethodVisitorWrapper {

        private static final String CALLBACK_SLOT_PLACEHOLDER = "callbackSlotPlaceholder";

        private final int callbackSlot;


        public TargetMethodWrapper(int callbackSlot) {
            this.callbackSlot = callbackSlot;
        }


        /**
         *  {@inheritDoc}
         */
        @Override
        public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
                MethodVisitor methodVisitor, Context implementationContext, TypePool typePool, int writerFlags,
                int readerFlags) {

            return new TargetMethodVisitor(methodVisitor, callbackSlot);
        }


        /**
         * ASM {@link MethodVisitor} that patches the callback slot constant in the
         * INDY bootstrap arguments of the target method.
         */
        static class TargetMethodVisitor extends MethodVisitor {

            private final int callbackSlot;


            public TargetMethodVisitor(MethodVisitor methodVisitor, int callbackSlot) {
                super(ASM9, methodVisitor);

                this.callbackSlot = callbackSlot;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                    Object... bootstrapMethodArguments) {
                if (bootstrapMethodHandle.getName().equals(AopWeaver.BOOTSTRAP_DISPATCHER_CALLBACK_METHOD.getName())
                        && bootstrapMethodHandle.getOwner().equals(BOOTSTRAP_DISPATCHER_TYPE.getInternalName())
                        && CALLBACK_SLOT_PLACEHOLDER.equals(bootstrapMethodArguments[0])) {
                    bootstrapMethodArguments[0] = callbackSlot;
                }

                super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
            }
        }
    }
}
