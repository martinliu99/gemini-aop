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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.factory.support.AdviceSpec.AspectJAdviceSpec;
import io.gemini.aop.factory.support.AdviceSpec.ByteBuddyAdviceSpec;
import io.gemini.aop.factory.support.AdviceSpec.PojoAdviceSpec;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.aspectj.weaver.PointcutParameter.ParameterCategory;
import io.gemini.core.util.ClassUtils;
import io.gemini.core.util.MethodUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Matches target method with advice method, including bound or assigned target object, 
 * arguments, return value or thrown exception.
 *
 * @author   martin.liu
 */
public interface AdviceMatcher extends ElementMatcher<MethodDescription> {

    /** 
     * {@inheritDoc}
     */
    @Override
    boolean matches(MethodDescription targetMethod);


    /**
     * Abstract base providing common logic for advice method validation: type visbility, 
     * type equality, or type assignment.
     */
    abstract class AbstractBase<A extends AdviceSpec> implements AdviceMatcher {

        protected static final Logger LOGGER = LoggerFactory.getLogger(AdviceMatcher.class);

        protected static final Generic RUNTIME_EXCEPTION = TypeDescription.ForLoadedType.of(RuntimeException.class).asGenericType();


        private final A adviceSpec;


        public AbstractBase(A adviceSpec) {
            this.adviceSpec = adviceSpec;
        }


        protected A getAdviceSpec() {
            return adviceSpec;
        }


        /**
         * Tests the adviceType is visible to the referred target Type.
         * @param operatedElement  the element of advice method
         * @param adviceType  the advice type defining advice method
         * @param ignoreAdviceType  ignore advice type or not
         * @param adviceMethod  the advice method
         * @param targetMethod the target method
         * @param referredType  the referred type
         * 
         * @return visible or not
         */
        protected boolean isTypeVisible(String operatedElement, TypeDescription adviceType, boolean ignoreAdviceType, 
                MethodDescription adviceMethod, MethodDescription targetMethod, TypeDescription referredType) {
            if (ClassUtils.isVisibleTo(referredType, adviceType))
                return true;

            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Ignored {} ({}) referring to invisible type (public or protected in the same package). \n"
                        + "  DeclaringType: {} \n"
                        + "  AdviceMethod: {} \n"
                        + "  TargetMethod: {} \n"
                        + "    ReferredType: {} {} \n",
                        ignoreAdviceType ? "advice class with method" : "advice method", operatedElement, 
                        adviceType.getTypeName(),
                        MethodUtils.getMethodSignature(adviceMethod),
                        referredType.getVisibility(), referredType.getTypeName()
                );

            return false;
        }

        /**
         * Tests the adviceMethodElementType is equal to the targetMethodElementType.
         * @param operatedElement  the element of advice method
         * @param adviceType  the advice type defining advice method
         * @param ignoreAdviceType  ignore advice type or not
         * @param adviceMethod  the advice method
         * @param adviceMethodElementType  the element type of advice method
         * @param targetMethod  the target method
         * @param targetMethodElementType  the element type of target method
         * @return equals or not
         */
        protected boolean isTypeEquals(String operatedElement, TypeDefinition adviceType, boolean ignoreAdviceType, 
                MethodDescription adviceMethod, Generic adviceMethodElementType, 
                MethodDescription targetMethod, Generic targetMethodElementType) {
            if (ClassUtils.containsTypeVariable(targetMethodElementType)
                    ? ClassUtils.equals(adviceMethodElementType.asErasure(), targetMethodElementType.asErasure())
                    : ClassUtils.equals(adviceMethodElementType, targetMethodElementType))
                return true;

            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Ignored {} ({}) referring to unequal type. \n" 
                        + "  DeclaringType: {} \n"
                        + "  AdviceMethod: {} \n"
                        + "    AssignedType: {} \n"
                        + "  TargetMethod: {} \n"
                        + "    SourceType: {} \n",
                        ignoreAdviceType ? "advice class with method" : "advice method", operatedElement,
                        adviceType.getTypeName(),
                        MethodUtils.getMethodSignature(adviceMethod), adviceMethodElementType.getTypeName(), 
                        MethodUtils.getMethodSignature(targetMethod), targetMethodElementType.getTypeName()
                );

            return false;
        }

        /**
         * Tests the adviceMethodElementType is assignment from the collection of targetMethodElementTypes.
         * @param operatedElement  the element of advice method
         * @param ignoreAdviceType  ignore advice type or method
         * @param isBindingOperation  binding or assigning operation
         * @param adviceMethod  the advice method
         * @param adviceMethodElementType  the element type of advice method
         * @param targetMethod  the target method
         * @param targetMethodMatchingTypes  collection of element type of target method
         * @return assignable or not
         */
        protected boolean isTypeAssignable(String operatedElement, boolean ignoreAdviceType, boolean isBindingOperation, 
                MethodDescription adviceMethod, List<Generic> adviceMethodMatchingTypes, 
                MethodDescription targetMethod, List<Generic> targetMethodMatchingTypes) {
            List<Generic> lhsTypes = isBindingOperation ? adviceMethodMatchingTypes : targetMethodMatchingTypes;
            List<Generic> rhsTypes = isBindingOperation ? targetMethodMatchingTypes : adviceMethodMatchingTypes;
            boolean anyMatched = false;
            for (Generic lhsType : lhsTypes) {
                lhsType = ClassUtils.containsTypeVariable(lhsType) ? lhsType.asRawType() : lhsType;

                boolean allMatched = true;
                for (Generic rhsType : rhsTypes) {
                    if (!ClassUtils.isAssignableFrom(lhsType, rhsType, false)) {
                        allMatched = false;
                        break;
                    }
                }

                if (allMatched)
                    anyMatched = true;
            }

            if (anyMatched)
                return true;

            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Ignored {} ({} {}) referring to unassignable type. \n" 
                        + "  {}: {} \n"
                        + "    {}{}: {} \n"
                        + "  {}: {} \n"
                        + "    Source{}: {} \n",
                        ignoreAdviceType ? "advice class with method" : "advice method", 
                        isBindingOperation ? "Binding" : "Assigning", operatedElement,
                        isBindingOperation ? "AdviceMethod" : "TargetMethod", MethodUtils.getMethodSignature(adviceMethod), 
                        isBindingOperation ? "Bound" : "Assigned", isBindingOperation || lhsTypes.size() == 1 ? "Type" : "Types", lhsTypes,
                        isBindingOperation ? "TargetMethod" : "AdviceMethod",  MethodUtils.getMethodSignature(targetMethod), 
                        isBindingOperation && rhsTypes.size() > 1 ? "Types" : "Type", rhsTypes
                );

            return false;
        }

        /**
         * Matches parameterizedReturnType and parameterizedExceptionType type with target class loader at class loading time.
         * 
         * @param adviceType  advice type defining advice method
         * @param adviceMethod  advice method
         * @param parameterizedReturnType  return type defined in parameterized {@link MutableJoinpoint} argument 
         * @param parameterizedExceptionType  exception type defined in parameterized {@link MutableJoinpoint} argument
         * @param targetMethod  target method
         * 
         * @return matched or not
         */
        protected boolean matcheParameterizedJoinpoint(TypeDescription adviceType, MethodDescription adviceMethod, 
                Generic parameterizedReturnType, Generic parameterizedExceptionType, MethodDescription targetMethod) {
            // verify return and throw type arguments of joinpoint with target method
            if (parameterizedReturnType != null) {
                String operatedElement = "ParameterizedReturnType";

                if (!isTypeVisible(operatedElement, adviceType, false, 
                        adviceMethod, targetMethod, parameterizedReturnType.asErasure()))
                    return false;

                Generic targetReturnType = targetMethod.getReturnType();
                if (!isTypeEquals(operatedElement, adviceType, false, 
                        adviceMethod, parameterizedReturnType, targetMethod, targetReturnType))
                    return false;
            }

            if (parameterizedExceptionType != null) {
                String operatedElement = "ParameterizedExceptionType";

                if (!isTypeVisible(operatedElement, adviceType, false, 
                        adviceMethod, targetMethod, parameterizedExceptionType.asErasure()))
                    return false;

                TypeList.Generic exceptionTypes = targetMethod.getExceptionTypes();
                if (!isTypeAssignable(operatedElement, false, true, 
                        adviceMethod, Collections.singletonList(parameterizedExceptionType), 
                        targetMethod, exceptionTypes.size() == 0 ? Collections.singletonList(RUNTIME_EXCEPTION) : exceptionTypes))
                    return false;
            }

            return true;
        }

        protected boolean matcheMutableJoinpoint(TypeDescription adviceType, MethodDescription adviceMethod, 
                Generic mutableJoinpointType, MethodDescription targetMethod) {
            if (mutableJoinpointType == null)
                return true;

            Generic parameterizedReturnType = mutableJoinpointType.getTypeArguments().get(0);
            // verify return and throw type arguments of joinpoint with target method
            if (parameterizedReturnType != null) {
                String operatedElement = "ParameterizedReturnType";

                if (!isTypeVisible(operatedElement, adviceType, false, 
                        adviceMethod, targetMethod, parameterizedReturnType.asErasure()))
                    return false;

                Generic targetReturnType = targetMethod.getReturnType();
                if (!isTypeEquals(operatedElement, adviceType, false, 
                        adviceMethod, parameterizedReturnType, targetMethod, targetReturnType))
                    return false;
            }

            Generic parameterizedExceptionType = mutableJoinpointType.getTypeArguments().get(1);
            if (parameterizedExceptionType != null) {
                String operatedElement = "ParameterizedExceptionType";

                if (!isTypeVisible(operatedElement, adviceType, false, 
                        adviceMethod, targetMethod, parameterizedExceptionType.asErasure()))
                    return false;

                TypeList.Generic exceptionTypes = targetMethod.getExceptionTypes();
                if (!isTypeAssignable(operatedElement, false, true, 
                        adviceMethod, Collections.singletonList(parameterizedExceptionType), 
                        targetMethod, exceptionTypes.size() == 0 ? Collections.singletonList(RUNTIME_EXCEPTION) : exceptionTypes))
                    return false;
            }

            return true;
        }
    }


    class PojoAdviceMatcher extends AbstractBase<PojoAdviceSpec.Loaded> {


        public PojoAdviceMatcher(PojoAdviceSpec.Loaded adviceSpec) {
            super(adviceSpec);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(MethodDescription targetMethod) {
            // verify return and throw type arguments of joinpoint with target method
            PojoAdviceSpec.Loaded adviceSpec = getAdviceSpec();
            return matcheMutableJoinpoint(adviceSpec.getDeclaringType(), adviceSpec.getAdviceMethod(), adviceSpec.getMutableJoinpointType(), targetMethod);
        }
    }


    class AspectJAdviceMatcher extends AbstractBase<AspectJAdviceSpec.Loaded> {

        protected AspectJAdviceMatcher(AspectJAdviceSpec.Loaded adviceSpec) {
            super(adviceSpec);
        }


        /**
         * {@inheritDoc}
         * 
         * It's necessary to verify target types' visibility since aspect advice class created by framework under aspect class loader
         * and could only access public target type or protected target type under same package.
         */
        @Override
        public boolean matches(MethodDescription targetMethod) {
            // 1.verify return and throw type arguments of joinpoint with target method
            AspectJAdviceSpec.Loaded adviceSpec = getAdviceSpec();
            if (!matcheMutableJoinpoint(adviceSpec.getDeclaringType(), adviceSpec.getAdviceMethod(), adviceSpec.getMutableJoinpointType(), targetMethod))
                return false;

            // 2.verify parameter type of advice method signature with target method
            if (!matchesAdviceMethodParameterTypes(targetMethod))
                return false;

            return true;
        }

        /**
         * Matches parameter types of advice method with target class loader at class loading time.
         * 
         * @param targetMethod  target method in matching
         * @return matched or not
         */
        private boolean matchesAdviceMethodParameterTypes(MethodDescription targetMethod) {
            AspectJAdviceSpec.Loaded adviceSpec = getAdviceSpec();
            TypeDescription declaringType = adviceSpec.getDeclaringType();
            MethodDescription adviceMethod = adviceSpec.getAdviceMethod();

            ParameterList<ParameterDescription.InDefinedShape> targetMethodParameters = targetMethod.asDefined().getParameters();
            for (ResolvedParameter resolvedParameter : adviceSpec.getResolvedParameters()) {
                ParameterCategory parameterCategory = resolvedParameter.getParameterCategory();
                if (ParameterCategory.JOINPOINT_PARAM == parameterCategory || ParameterCategory.MUTABLE_JOINPOINT_PARAM == parameterCategory
                        || ParameterCategory.PROCEDDING_JOINPOINT_PARAM == parameterCategory || ParameterCategory.STATIC_PART_PARAM == parameterCategory)
                    continue;

                // analyze target method parameter's binding type
                String operatedElement = null;
                Generic adviceMethodMatchingType = resolvedParameter.getParameterType();
                List<Generic> targetMethodMatchingTypes = null;

                if (ParameterCategory.THIS_VAR == parameterCategory || ParameterCategory.TARGET_VAR == parameterCategory) {
                    operatedElement = "TargetObject";
                    targetMethodMatchingTypes = Collections.singletonList( targetMethod.getDeclaringType().asGenericType() );
                } else if (ParameterCategory.ARGS_VAR == parameterCategory) {
                    ParameterDescription parameter = targetMethodParameters.get(resolvedParameter.getArgsIndex());

                    operatedElement = "Argument-" + parameter.getName();
                    targetMethodMatchingTypes = Collections.singletonList( parameter.getType().asGenericType() );
                } else if (ParameterCategory.RETURNING_ANNOTATION == parameterCategory) {
                    operatedElement = "ReturnValue";
                    targetMethodMatchingTypes = Collections.singletonList( targetMethod.getReturnType().asGenericType() );
                } else if (ParameterCategory.THROWING_ANNOTATION == parameterCategory) {
                    operatedElement = "ThrownException";
                    targetMethodMatchingTypes = targetMethod.getExceptionTypes();
                }

                // verify parameter type's visibility
                if (operatedElement != null && !isTypeVisible(operatedElement, declaringType, false, 
                        adviceMethod, targetMethod, adviceMethodMatchingType.asErasure()))
                    return false;

                // verify parameter type's assignment
                if (operatedElement != null && !isTypeAssignable(operatedElement, false, true, 
                        adviceMethod, Collections.singletonList(resolvedParameter.getParameterType()), targetMethod, targetMethodMatchingTypes))
                    return false;
            }

            return true;
        }
    }


    class ByteBuddyAdviceMatcher extends AbstractBase<ByteBuddyAdviceSpec.Loaded> {

        private static final String NO_EXCEPTION_HANDLER_CLASS = "net.bytebuddy.asm.Advice$NoExceptionHandler";

        protected ByteBuddyAdviceMatcher(ByteBuddyAdviceSpec.Loaded adviceSpec) {
            super(adviceSpec);
        }

        /**
         * {@inheritDoc}
         * 
         * It's unnecessary to verify target types' visibility since java compiler helps to validate it
         * when compiling ByteBuddy Advice class.
         */
        @Override
        public boolean matches(MethodDescription targetMethod) {
            Map<MethodDescription, Boolean> adviceMethods = new HashMap<>(2);
            ByteBuddyAdviceSpec.Loaded adviceSpec = getAdviceSpec();
            if (adviceSpec.getEnterAdviceMethod() != null)
                adviceMethods.put(adviceSpec.getEnterAdviceMethod(), true);
            if (adviceSpec.getExitAdviceMethod() != null)
                adviceMethods.put(adviceSpec.getExitAdviceMethod(), false);

            for (Entry<MethodDescription, Boolean> entry : adviceMethods.entrySet()) {
                MethodDescription adviceMethod = entry.getKey();

                // verify target method
                if (!matchesTargetConstructor(targetMethod, adviceMethod))
                    return false;

                // verify parameters of advice method
                if (!matchesAdviceMethodParameterTypes(targetMethod, adviceMethod))
                    return false;

                // verify return value of advice method
                if (!matchesAdviceMethodReturnType(targetMethod, entry.getValue(), adviceMethod))
                    return false;
            }

            return true;
        }

        private boolean matchesTargetConstructor(MethodDescription targetMethod, MethodDescription adviceMethod) {
            if (!targetMethod.isConstructor()) 
                return true;

            // skip constructor execution
            AnnotationDescription.Loadable<OnMethodEnter> onMethodEnterAnnotation = adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class);
            if (onMethodEnterAnnotation != null) {
                TypeDescription skipOnType = (TypeDescription) onMethodEnterAnnotation.getValue("skipOn").resolve();
                if (!skipOnType.represents(void.class)) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ignored advice class with enter advice method skipping constructor code execution. \n"
                                + "  DeclaringType: {} \n"
                                + "  AdviceMethod: {} \n"
                                + "  TargetMethod: {} \n",
                                adviceMethod.getDeclaringType().getTypeName(),
                                MethodUtils.getMethodSignature(adviceMethod),
                                MethodUtils.getMethodSignature(targetMethod)
                        );

                    return false;
                }
            }

            // catch exception during constructor call
            AnnotationDescription.Loadable<OnMethodExit> onMethodExitAnnotation = adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class);
            if (onMethodExitAnnotation != null) {
                TypeDescription OnThrowableType = (TypeDescription) onMethodExitAnnotation.getValue("onThrowable").resolve();
                if (!OnThrowableType.getTypeName().equals(NO_EXCEPTION_HANDLER_CLASS)) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ignored advice class with exit advice method catching exception during constructor call. \n"
                                + "  DeclaringType: {} \n"
                                + "  AdviceMethod: {} \n"
                                + "  TargetMethod: {} \n",
                                adviceMethod.getDeclaringType().getTypeName(),
                                MethodUtils.getMethodSignature(adviceMethod),
                                MethodUtils.getMethodSignature(targetMethod)
                        );

                    return false;
                }
            }

            return true;
        }

        private boolean matchesAdviceMethodParameterTypes(MethodDescription targetMethod, MethodDescription adviceMethod) {
            ParameterList<?> targetMethodParameters = targetMethod.getParameters();
            for (ParameterDescription parameterDescription : adviceMethod.getParameters()) {
                String operatedElement = null;
                Generic adviceMethodMatchingType = parameterDescription.getType();
                List<Generic> targetMethodMatchingTypes = null;

                // analyze target method element's binding type
                AnnotationList parameterAnnotations = parameterDescription.getDeclaredAnnotations();
                if (parameterAnnotations.isAnnotationPresent(Advice.This.class)) {
                    operatedElement = "TargetObject";
                    targetMethodMatchingTypes = Collections.singletonList( targetMethod.getDeclaringType().asGenericType() );
                } else if (parameterAnnotations.isAnnotationPresent(Advice.Argument.class)) {
                    int argIndex = parameterAnnotations.ofType(Advice.Argument.class).load().value();
                    if (argIndex < 0 || argIndex >= targetMethod.getParameters().size()) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored advice class with method annoating @Advice.Argument(value = {}). \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "  TargetMethod: {} \n",
                                    argIndex,
                                    adviceMethod.getDeclaringType().getTypeName(),
                                    MethodUtils.getMethodSignature(adviceMethod),
                                    MethodUtils.getMethodSignature(targetMethod)
                            );

                        return false;
                    }

                    ParameterDescription parameter = targetMethodParameters.get(argIndex);

                    operatedElement = "Argument-" + parameter.getName();
                    targetMethodMatchingTypes = Collections.singletonList( parameter.getType().asGenericType() );
                } else if (parameterAnnotations.isAnnotationPresent(Advice.Return.class)) {
                    operatedElement = "ReturnValue";
                    targetMethodMatchingTypes = Collections.singletonList( targetMethod.isConstructor()
                            ? targetMethod.getDeclaringType().asGenericType()
                            : targetMethod.getReturnType().asGenericType() );
                } else if (parameterAnnotations.isAnnotationPresent(Advice.Thrown.class)) {
                    operatedElement = "ThrownExceptions";
                    targetMethodMatchingTypes = targetMethod.getExceptionTypes();

                    // verify onThrowable type name
                    TypeDescription onThrowableType = (TypeDescription) adviceMethod.getDeclaredAnnotations()
                            .ofType(OnMethodExit.class)
                            .getValue("onThrowable")
                            .resolve();
                    if (onThrowableType.getTypeName().equals(NO_EXCEPTION_HANDLER_CLASS))
                        continue;
                    if (!isTypeEquals(operatedElement, adviceMethod.getDeclaringType(), true, 
                            adviceMethod, adviceMethodMatchingType, targetMethod, onThrowableType.asGenericType()))
                        return false;
                }

                // verify parameter type's visibility
                if (operatedElement != null && !isTypeVisible(operatedElement, adviceMethod.getDeclaringType().asErasure(), true, 
                        adviceMethod, targetMethod, adviceMethodMatchingType.asErasure()))
                    return false;

                // verify parameter type's assignment
                if (operatedElement != null && !isTypeAssignable(operatedElement, true, true, 
                        adviceMethod, Collections.singletonList(adviceMethodMatchingType), targetMethod, targetMethodMatchingTypes))
                    return false;
            }

            return true;
        }

        private boolean matchesAdviceMethodReturnType(MethodDescription targetMethod, boolean isEnterAdviceMethod, MethodDescription adviceMethod) {
            Generic adviceMethodReturnType = adviceMethod.getReturnType();
            if (adviceMethodReturnType.represents(void.class))
                return true;

            AnnotationList methodAnnotations = adviceMethod.getDeclaredAnnotations();
            if (adviceMethodReturnType.isArray() && !methodAnnotations.isAnnotationPresent(Advice.AssignReturned.AsScalar.class))
                return true;

            String operatedElement = null;
            List<Generic> targetMethodMatchingTypes = null;
            Generic adviceMethodMatchingType = adviceMethodReturnType;

            // analyze target method element's assigning type
            if (methodAnnotations.isAnnotationPresent(Advice.AssignReturned.ToThis.class)) {
                if (targetMethod.isStatic()) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ignored advice class with method assigning this reference for static target method. \n"
                                + "  DeclaringType: {} \n"
                                + "  AdviceMethod: {} \n"
                                + "  TargetMethod: {} \n",
                                adviceMethod.getDeclaringType().getTypeName(),
                                MethodUtils.getMethodSignature(adviceMethod),
                                MethodUtils.getMethodSignature(targetMethod)
                        );

                    return false;
                }

                if (targetMethod.isConstructor() && isEnterAdviceMethod) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ignored advice class with method assigning this reference in constructor prior to initialization. \n"
                                + "  DeclaringType: {} \n"
                                + "  AdviceMethod: {} \n"
                                + "  TargetMethod: {} \n",
                                adviceMethod.getDeclaringType().getTypeName(),
                                MethodUtils.getMethodSignature(adviceMethod),
                                MethodUtils.getMethodSignature(targetMethod)
                        );

                    return false;
                }

                operatedElement = "TargetObject";
                targetMethodMatchingTypes = Collections.singletonList( targetMethod.getDeclaringType().asGenericType());
            } else if (methodAnnotations.isAnnotationPresent(Advice.AssignReturned.ToArguments.class)) {
                Advice.AssignReturned.ToArguments.ToArgument[] toArguments = methodAnnotations.ofType(Advice.AssignReturned.ToArguments.class).load().value();
                if (toArguments == null || toArguments.length > 1)
                    return true;

                int argIndex = toArguments[0].value();
                if (argIndex < 0 || argIndex >= targetMethod.getParameters().size()) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Ignored advice class with method annoating @AssignReturned.ToArguments( {ToArgument(value = {})} ). \n"
                                + "  DeclaringType: {} \n"
                                + "  AdviceMethod: {} \n"
                                + "  TargetMethod: {} \n",
                                argIndex,
                                adviceMethod.getDeclaringType().getTypeName(),
                                MethodUtils.getMethodSignature(adviceMethod),
                                MethodUtils.getMethodSignature(targetMethod)
                        );

                    return false;
                }

                ParameterDescription parameter = targetMethod.getParameters().get(argIndex);

                operatedElement = "Argument-" + parameter.getName();
                targetMethodMatchingTypes = Collections.singletonList( parameter.getType());
            } else if (methodAnnotations.isAnnotationPresent(Advice.AssignReturned.ToReturned.class)) {
                operatedElement = "ReturnValue";
                targetMethodMatchingTypes = Collections.singletonList( targetMethod.getReturnType());
            } else if (methodAnnotations.isAnnotationPresent(Advice.AssignReturned.ToThrown.class)) {
                operatedElement = "ThrownException";
                targetMethodMatchingTypes = new ArrayList<>( targetMethod.getExceptionTypes() ); 
                targetMethodMatchingTypes.add(RUNTIME_EXCEPTION);
            }

            // verify parameter type's visibility
            if (operatedElement != null && !isTypeVisible(operatedElement, adviceMethod.getDeclaringType().asErasure(), true, 
                    adviceMethod, targetMethod, adviceMethodMatchingType.asErasure()))
                return false;

            // verify parameter type's assignment
            if (operatedElement != null && !isTypeAssignable(operatedElement, true, false, 
                    adviceMethod, Collections.singletonList( adviceMethodMatchingType), targetMethod, targetMethodMatchingTypes))
                return false;

            return true;
        }
    }
}
