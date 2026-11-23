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
package io.gemini.aop.weaver;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.Advisor;
import io.gemini.aop.AopContext;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Advice.After;
import io.gemini.api.aop.Advice.Around;
import io.gemini.api.aop.Advice.Before;
import io.gemini.api.aop.Joinpoint;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.Joinpoint.ProceedingJoinpoint;
import io.gemini.core.bootstrap.BootstrapClassConsumer;
import io.gemini.core.util.Assert;
import io.gemini.core.util.ClassUtils;
import io.gemini.core.util.CollectionUtils;
import io.gemini.core.util.ObjectUtils;
import io.gemini.core.util.StringUtils;
import io.gemini.core.util.Throwables;

/**
 * Holds joinpoint metadata and runtime dispatch logic for the Gemini AOP framework.
 * <p>
 * Key inner classes:
 * <ul>
 *   <li>{@link Descriptor} – immutable metadata about a joinpoint (target class, method/constructor,
 *       advisors). Created once per instrumented method and cached.</li>
 *   <li>{@link DefaultMutableJoinpoint} – mutable joinpoint passed to before/after advice,
 *       allowing argument modification and return/throw override.</li>
 *   <li>{@link MutableJoinpointDispatcher} – drives the before/after advices at runtime.</li>
 *   <li>{@link DefaultProceedingJoinpoint} – joinpoint passed to around advice, supporting
 *       {@code proceed()} to invoke the original method.</li>
 *   <li>{@link ProceedingJoinpointDispatcher} – drives the around advices at runtime.</li>
 * </ul>
 * </p>
 *
 * @author   martin.liu
 */
public interface Joinpoints {

    /**
     * Immutable descriptor capturing all static metadata about an instrumented joinpoint.
     * Cached per method/constructor to avoid repeated reflection lookups at runtime.
     */
    class Descriptor {

        private final Lookup targetLookup;

        private final String accessibleName;
        private final AccessibleObject accessibleObject;

        private final boolean typeInitializer;
        private final boolean constructor;

        private final boolean staticMethod;

        private final boolean voidReturn;

        private Class<?>[] exceptionTypes;

        // refresh at runtime
        private List<? extends Advisor> advisors;


        /**
         * Creates a new {@code Descriptor} for the given joinpoint.
         *
         * @param thisLookup       the lookup context of the instrumented class
         * @param accessibleName   the string representation of the method or constructor
         * @param accessibleObject the target {@link Method} or {@link Constructor}, or {@code null} for type initializers
         * @param advisors     the list of advisors to apply at this joinpoint
         */
        public Descriptor(Lookup thisLookup, String accessibleName, AccessibleObject accessibleObject, 
                List<? extends Advisor> advisors) {
            this.targetLookup = thisLookup;

            this.accessibleName = accessibleName;
            this.accessibleObject = accessibleObject;

            if (accessibleObject == null) {
                this.typeInitializer = true;

                this.constructor = false;
                this.staticMethod = true;
                this.voidReturn = true;
            } else {
                this.typeInitializer = false;

                if (accessibleObject instanceof Constructor) {
                    this.constructor = true;
                    this.staticMethod = false;
                    this.voidReturn = true;
                } else {
                    this.constructor = false;

                    Method method = (Method) accessibleObject;
                    this.staticMethod = Modifier.isStatic(method.getModifiers());
                    this.voidReturn = method.getReturnType() == void.class;
                }
            }

            this.advisors = advisors;
        }


        /**
         * Returns the {@link java.lang.invoke.MethodHandles.Lookup} for the instrumented class,
         * used to access private members of the target class.
         *
         * @return the target class lookup
         */
        public Lookup getTargetLookup() {
            return targetLookup;
        }

        /**
         * Returns the class being instrumented at this joinpoint.
         *
         * @return the target class
         */
        public Class<?> getTargetClass() {
            return targetLookup.lookupClass();
        }

        /**
         * Returns the string representation of the target method or constructor signature.
         *
         * @return the accessible name
         */
        public String getAccessibleName() {
            return accessibleName;
        }

        /**
         * Returns the target {@link Constructor}, or {@code null} if this is not a constructor joinpoint.
         *
         * @return the target constructor, or {@code null}
         */
        public Constructor<?> getTargetConstructor() {
            return (this.typeInitializer || !this.constructor) ? null : (Constructor<?>) this.getStaticPart();
        }

        /**
         * Returns the target {@link Method}, or {@code null} if this is not a method joinpoint.
         *
         * @return the target method, or {@code null}
         */
        public Method getTargetMethod() {
            return (this.typeInitializer || this.constructor) ? null : (Method) this.getStaticPart();
        }

        private AccessibleObject getStaticPart() {
            return this.accessibleObject;
        }

        /**
         * Returns {@code true} if this joinpoint is a static class initializer ({@code <clinit>}).
         *
         * @return {@code true} for type initializer joinpoint
         */
        public boolean isTypeInitializer() {
            return typeInitializer;
        }

        /**
         * Returns {@code true} if the target method is static.
         *
         * @return {@code true} for static method joinpoint
         */
        public boolean isStaticMethod() {
            return staticMethod;
        }

        /**
         * Returns {@code true} if this joinpoint is a constructor.
         *
         * @return {@code true} for constructor joinpoint
         */
        public boolean isConstructor() {
            return constructor;
        }

        /**
         * Returns {@code true} if the target method returns {@code void}.
         *
         * @return {@code true} for void-return joinpoint
         */
        public boolean isVoidReturn() {
            return voidReturn;
        }

        public Class<?>[] getExceptionTypes() {
            if (this.exceptionTypes != null)
                return this.exceptionTypes;

            Class<?>[] exceptionTypes = isTypeInitializer() 
                    ? new Class<?>[] {}
                    : isConstructor() 
                            ? getTargetConstructor().getExceptionTypes()
                            : getTargetMethod().getExceptionTypes();

            Class<?>[] matchingExceptionTypes = new Class<?>[exceptionTypes.length + 1];
            System.arraycopy(exceptionTypes, 0, matchingExceptionTypes, 0, exceptionTypes.length);
            matchingExceptionTypes[exceptionTypes.length] = RuntimeException.class;

            return this.exceptionTypes = matchingExceptionTypes;
        }

        /**
         * Returns the advisors applied at this joinpoint.
         *
         * @return the list of advisors
         */
        public List<? extends Advisor> getAdvisors() {
            return advisors;
        }
    }


    /**
     * Abstract base class for all joinpoint implementations, providing common state:
     * descriptor, target object, arguments, and invocation context.
     */
    abstract class AbstractBase<R, E extends Throwable> implements Joinpoint {

        protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractBase.class);

        protected static final Object UNDEFINED_RETURN_VALUE = new Object();
        @SuppressWarnings("StaticAssignmentOfThrowable")
        protected static final Throwable UNDEFINED_THROWN_EXCEPTION = new Throwable();


        private Descriptor descriptor;

        private Object targetObject;

        private Object[] arguments;

        private final Map<String, Object> invocationContext;


        AbstractBase(Descriptor descriptor, Object targetObject, Object[] arguments) {
            this.descriptor = descriptor;

            if (descriptor.isConstructor()) {
                // ignore input targetObject
                this.targetObject = null;
            } else {
                if (!descriptor.staticMethod) {
                    Assert.notNull(targetObject, "'targetObject' must not be null.");

                    this.targetObject = targetObject;
                } else {
                    this.targetObject = null;
                }
            }

            if (arguments == null || arguments.length == 0) {
                this.arguments = new Object[0];
            } else {
                this.arguments = arguments;
            }

            this.invocationContext = new LinkedHashMap<>();
        }

        /**
         * Returns the {@link Descriptor} holding static joinpoint metadata.
         *
         * @return the joinpoint descriptor
         */
        protected Descriptor getDescriptor() {
            return descriptor;
        }

        /**
         * get this class Lookup to access private member
         * @return lookup of target type
         */
        @Override
        public Lookup getTargetLookup() {
            return descriptor.getTargetLookup();
        }

        /**
         * get class information
         * @return target type
         */
        @Override
        public Class<?> getTargetClass() {
            return descriptor.getTargetClass();
        }

        /**
         * Returns the static part of this joinpoint.
         *
         * <p>The static part is an accessible object on which a chain of
         * interceptors are installed. */
        @Override
        public AccessibleObject getStaticPart() {
            return descriptor.getStaticPart();
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public Object getTargetObject() {
            return this.targetObject;
        }

        /**
         * Sets the target object of target method invocation.
         * 
         * @param targetObject the target object
         */
        protected void setTargetObjectInternal(Object targetObject) {
            this.targetObject = targetObject;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public Object[] getArguments() {
            Object[] arguments = new Object[this.arguments.length];
            System.arraycopy(this.arguments, 0, arguments, 0, this.arguments.length);
            return arguments;
        }

        /**
         * Sets arguments of target method invocation.
         * 
         * @param arguments the 
         */
        protected void setArgumentsInternal(Object... arguments) {
            this.arguments = arguments;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getInvocationContext(String key) {
            return this.invocationContext.get(key);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setInvocationContext(String key, Object value) {
            this.invocationContext.put(key, value);
        }
    }


    /**
     * Mutable joinpoint passed to {@link io.gemini.api.aop.Advice.Before} and
     * {@link io.gemini.api.aop.Advice.After} advice. Allows advice to inspect and
     * override the target method's return value or thrown exception.
     */
    class DefaultMutableJoinpoint<R, E extends Throwable> extends AbstractBase<R, E> implements MutableJoinpoint<R, E> {

        private final Supplier<? extends Advice> currentAdviceSupplier;

        private boolean hasAssignedReturnValue = false;

        @SuppressWarnings("unchecked")
        private R returnValue = (R) UNDEFINED_RETURN_VALUE;

        private boolean hasAssignedThrownException = false;

        @SuppressWarnings("unchecked")
        private E thrownException = (E) UNDEFINED_THROWN_EXCEPTION;


        public DefaultMutableJoinpoint(Descriptor descriptor, Object targetObject, 
                Object[] arguments, Supplier<? extends Advice> currentAdviceSupplier) {
            super(descriptor, targetObject, arguments);

            this.currentAdviceSupplier = currentAdviceSupplier;
        }

        private boolean isTypeAssignable(String operatedElement, Class<?> lhsTypes, Object rhsObject) {
            return isTypeAssignable(operatedElement, new Class<?>[] {lhsTypes}, rhsObject);
        }

        private boolean isTypeAssignable(String operatedElement, Class<?>[] lhsTypes, Object rhsObject) {
            Descriptor descriptor = getDescriptor();
            boolean matched = false;
            for (Class<?> lhsType : lhsTypes) {
                if (rhsObject == null) {
                    if (lhsType.isPrimitive()) 
                        break;
                    else {
                        matched = true;
                        continue;
                    }
                }
                if (ClassUtils.isAssignableFrom(lhsType, rhsObject.getClass(), rhsObject != null)) {
                    matched = true;
                }
            }
            if (matched)
                return true;

            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Ignored '{}' referring to unassignable type. \n"
                        + "  ClassLoader: {} \n"
                        + "  TargetMethod: {} \n"
                        + "    Types: {} \n"
                        + "  AdviceMethod: {} \n"
                        + "    AssignedObject: {} \n",
                        operatedElement, 
                        descriptor.getTargetClass().getClassLoader(),
                        descriptor.getAccessibleName(), lhsTypes, 
                        currentAdviceSupplier.get(), ObjectUtils.getObjectId(rhsObject)
                );

            return false;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public void setTargetObject(Object targetObject) {
            // validate target object
            Descriptor descriptor = getDescriptor();
            if (targetObject == null || descriptor.isStaticMethod()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Ignored 'this' reference assignment for static target method. \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  CurrentAdvice: {} \n",
                            descriptor.getTargetClass().getClassLoader(),
                            descriptor.getAccessibleName(),
                            currentAdviceSupplier.get()
                    );
                }
                return;
            }

            if (!isTypeAssignable("this", descriptor.getTargetClass(), targetObject))
                return;

            // assign target object
            super.setTargetObjectInternal(targetObject);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void setArguments(Object... arguments) {
            // validate arguments
            if (arguments == null)
                return;

            Descriptor descriptor = getDescriptor();
            Parameter[] parameters = descriptor.isTypeInitializer() 
                    ? new Parameter[] {}
                    : descriptor.isConstructor()
                        ? descriptor.getTargetConstructor().getParameters()
                        : descriptor.getTargetMethod().getParameters();
            if (arguments.length != parameters.length)
                return;

            for (int index = 0; index < arguments.length; index++) {
                Object argument = arguments[index];
                Parameter parameter = parameters[index];
                if (!isTypeAssignable("Argument-" + parameter.getName(), parameter.getType(), argument))
                    return;
            }

            // assign arguments
            super.setArgumentsInternal(arguments);
        }


        /**
         * {@inheritDoc}
         * 
         * <p>Throws {@link IllegalStateException} if the return value has not been set yet,
         * such as calling in {@link io.gemini.api.aop.Advice.Before}.</p>
         */
        @Override
        public R getReturnValue() throws IllegalStateException {
            if (UNDEFINED_RETURN_VALUE == this.returnValue)
                throw new IllegalStateException("Uninitialized or unsupported return value.");

            return returnValue;
        }

        /**
         * Sets the return value that advice wants to substitute for the target method's result.
         * Validates that the value is assignment-compatible with the method's declared return type.
         * Ignored with a warning if the type is incompatible.
         *
         * @param returnValue the overridden return value to set
         */
        @Override
        public void setReturnValue(R returnValue) {
            // validate return value
            Descriptor descriptor = getDescriptor();
            if (descriptor.isVoidReturn()) {
                assignReturnValueInternal(null);
                return;
            }

            Class<?> returnType = descriptor.isTypeInitializer() || descriptor.isConstructor() 
                    ? void.class
                    : descriptor.getTargetMethod().getReturnType();
            if (returnValue == null || !isTypeAssignable("ReturnValue", returnType, returnValue))
                return;

            // assign return value
            assignReturnValueInternal(returnValue);
        }

        private void assignReturnValueInternal(R returnValue) {
            this.hasAssignedReturnValue = true;
            this.returnValue = returnValue;

            this.hasAssignedThrownException = false;
            this.thrownException = null;
        }

        /**
         * Initializes the actual return value from the target method invocation.
         *
         * @param returnValue the return value from the target method
         */
        protected void setReturnValueInternal(R returnValue) {
            this.returnValue = returnValue;
        }

        /** 
         * Returns {@code true} if advice has set a return value. 
         */
        protected boolean hasAssignedReturnValue() {
            return hasAssignedReturnValue;
        }


        /**
         * {@inheritDoc}
         * 
         * <p>Throws {@link IllegalStateException} if the exception has not been set yet,
         * such as calling in {@link io.gemini.api.aop.Advice.Before}.</p>
         */
        @Override
        public E getThrownException() throws IllegalStateException {
            if (UNDEFINED_THROWN_EXCEPTION == this.thrownException)
                throw new IllegalStateException("Uninitialized thrown exception.");

            return thrownException;
        }

        /**
         * Sets the exception that advice wants to substitute for the target method's thrown exception.
         * 
         * Validates that the exception is a {@link RuntimeException} or a declared checked exception.
         * Ignored with a warning if the type is incompatible.
         *
         * @param thrownException the override exception to set; must not be {@code null}
         */
        @Override
        public void setThrownException(E thrownException) {
            // validate thrown exception
            if (thrownException == null) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ignored null thrown exception.\n");

                return;
            }

            if (!isTypeAssignable("ThrownException", getDescriptor().getExceptionTypes(), thrownException))
                return;

            // assign thrown exception
            this.assignThrownExceptionInternal(thrownException);
        }

        private void assignThrownExceptionInternal(E thrownException) {
            this.hasAssignedThrownException = true;
            this.thrownException = thrownException;

            this.hasAssignedReturnValue = false;
            this.returnValue = null;
        }

        /**
         * Sets the actual exception thrown by the target method.
         *
         * @param thrownException the exception thrown, or {@code null} if none
         */
        protected void setThrownExceptionInternal(E thrownException) {
            this.thrownException = thrownException;
        }

        /** 
         * Returns {@code true} if advice has set an exception. 
         */
        protected boolean hasAssignedThrownException() {
            return hasAssignedThrownException;
        }
    }


    /**
     * Runtime dispatcher for before advice, and after advice chains.
     * Created by the {@link io.gemini.aop.weaver.BootstrapDispatcher.Delegator} at each
     * instrumented joinpoint invocation and drives the {@link DefaultMutableJoinpoint}.
     */
    @BootstrapClassConsumer
    class MutableJoinpointDispatcher<R, E extends Throwable> implements BootstrapDispatcher.Dispatcher<R, E>, Supplier<Advice> {

        private static final Logger LOGGER = LoggerFactory.getLogger(MutableJoinpointDispatcher.class);


        private final AopContext aopContext;

        private DefaultMutableJoinpoint<R, E> joinpoint = null;

        private boolean dispatchBeforeAdvice = true;
        private List<? extends Advice.Before<R, E>> beforeAdvices = Collections.emptyList();
        private List<? extends Advice.After<R, E>> afterAdvices = Collections.emptyList();

        private Advice currentAdvice;


        /**
         * Creates a new {@code MutableJoinpointDispatcher} for the given joinpoint.
         * Initializes the joinpoint instance and splits the advisor chain into before/after lists.
         *
         * @param descriptor    the joinpoint descriptor
         * @param targetObject  the target object instance
         * @param arguments     the method arguments
         * @param aopContext    the central AOP context
         */
        public MutableJoinpointDispatcher(Descriptor descriptor, Object targetObject, 
                Object[] arguments, AopContext aopContext) {
            Assert.notNull(aopContext, "'aopContext' must not be null.");
            this.aopContext = aopContext;


            Class<?> targetClass = descriptor.getTargetClass();
            ClassLoader targetClassLoader = targetClass.getClassLoader();
            try {
                doInitialize(descriptor, targetObject, arguments);
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Could not create joinpoint instance of target type '{}',"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n", 
                            targetClass.getName(), 
                            targetClassLoader, 
                            descriptor.getAccessibleName(), t);

                Throwables.throwIfRequired(t);
            }
        }


        /**
         * Initializes the joinpoint and splits the advisor chain into before/after advice lists.
         *
         * @param descriptor    the joinpoint descriptor
         * @param targetObject  the target object instance
         * @param arguments     the method arguments
         */
        @SuppressWarnings("unchecked")
        protected void doInitialize(Descriptor descriptor, Object targetObject, Object[] arguments) {
            // create Joinpoint instance
            joinpoint = new DefaultMutableJoinpoint<R, E>(descriptor, targetObject, arguments, this);

            // initialize advisors
            List<? extends Advisor> advisors = descriptor.getAdvisors();
            advisors = CollectionUtils.isEmpty(advisors) ? Collections.emptyList() : advisors;

            List<Advice.Before<R, E>> beforeAdvices = new ArrayList<>();
            List<Advice.After<R, E>> afterAdvices = new ArrayList<>();
            for (Iterator<? extends Advisor> iterator = advisors.iterator(); iterator.hasNext(); ) {
                Advisor advisor = iterator.next();
                Class<? extends Advice> adviceClass = advisor.getAdviceClass();
                if (adviceClass == null) {
                    iterator.remove();

                    continue;
                }

                boolean isBeforeAdvice = Advice.Before.class.isAssignableFrom(adviceClass);
                boolean isAfterAdvice = Advice.After.class.isAssignableFrom(adviceClass);
                if (!isBeforeAdvice && !isAfterAdvice) {
                    continue;
                }

                Advice advice = advisor.getAdvice();
                if (advice == null) {
                    iterator.remove();

                    continue;
                }

                if (isBeforeAdvice && isAfterAdvice) {
                    beforeAdvices.add( (Advice.Before<R, E>) advice );
                    afterAdvices.add( (Advice.After<R, E>) advice );
                } else if (isBeforeAdvice && !isAfterAdvice) {
                    beforeAdvices.add( (Advice.Before<R, E>) advice );
                } else if (!isBeforeAdvice && isAfterAdvice) {
                    afterAdvices.add( (Advice.After<R, E>) advice );
                }
            }

            this.beforeAdvices = beforeAdvices;
            this.afterAdvices = afterAdvices;
        }


        protected AopContext getAopContext() {
            return aopContext;
        }

        protected List<? extends Advice.Before<R, E>> getBeforeAdvices() {
            return beforeAdvices;
        }

        protected List<? extends Advice.After<R, E>> getAfterAdvices() {
            return afterAdvices;
        }

        protected Class<?> getTargetClass() {
            return joinpoint.getDescriptor().getTargetClass();
        }

        protected Object getAccessibleName() {
            return joinpoint.getDescriptor().getAccessibleName();
        }


        /**
         * Dispatches the before or after advice chain depending on the current phase.
         * Before-advice is invoked first; after-advice is invoked on the second call.
         * Stops early if any advice sets an override return or throw value.
         *
         * @return always {@code null} for before/after advice
         * @throws E if any advice throws an unrecoverable exception
         */
        @Override
        public R dispatch() throws E {
            if (dispatchBeforeAdvice && this.beforeAdvices.size() == 0) {
                dispatchBeforeAdvice = false;
                return null;
            }
            if (!dispatchBeforeAdvice && this.afterAdvices.size() == 0) {
                return null;
            }

            String targetTypeName = getTargetClass().getName();
            ClassLoader targetClassLoader = getTargetClass().getClassLoader();
            try {
                if (dispatchBeforeAdvice) {
                    this.doInvokeBeforeAdvices(targetClassLoader);
                } else {
                    this.doInvokeAfterAdvices(targetClassLoader);
                }

                return null;
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("$Could not invoke {} for joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  Advices: \n"
                            + "    {} \n",
                            dispatchBeforeAdvice ? "BeforeAdvices" : "AfterAdvices", 
                            targetTypeName,
                            targetClassLoader, 
                            getAccessibleName(), 
                            StringUtils.join(dispatchBeforeAdvice ? this.beforeAdvices : this.afterAdvices, e -> e.getClass().getName(), "\n    "),
                            t
                    );

                Throwables.throwIfRequired(t);
                return null;
            }
        }

        /**
         * Invokes all before-advice instances in order, stopping early if any advice
         * sets an override return or throw value.
         *
         * @param targetClassLoader the target class loader (used for thread context)
         * @throws Throwable if any advice throws
         */
        protected void doInvokeBeforeAdvices(ClassLoader targetClassLoader) throws Throwable {
            for (Iterator<? extends Advice.Before<R, E>> it = beforeAdvices.listIterator(); it.hasNext(); ) {
                Before<R, E> advice = it.next();

                this.currentAdvice = advice;
                try {
                    advice.before(joinpoint);
                } catch (Throwable t) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("$Could not invoke joinpoint instance of target type '{}', \n"
                                + "  ClassLoader: {} \n"
                                + "  TargetMethod: {} \n"
                                + "  CurrentAdvice: {}", 
                                getTargetClass().getTypeName(),
                                targetClassLoader,
                                getAccessibleName(),
                                advice, 
                                t
                        );

                    Throwables.throwIfRequired(t);
                }
            }

            this.currentAdvice = null;
            this.dispatchBeforeAdvice = false;
        }

        /**
         * Invokes all after-advice instances in reverse order, stopping early if any advice
         * sets an override return or throw value.
         *
         * @param targetClassLoader the target class loader (used for thread context)
         * @throws Throwable if any advice throws
         */
        protected void doInvokeAfterAdvices(ClassLoader targetClassLoader) throws Throwable {
            for (ListIterator<? extends Advice.After<R, E>> it = afterAdvices.listIterator(afterAdvices.size()); it.hasPrevious(); ) {
                After<R, E> advice = it.previous();

                this.currentAdvice = advice;
                try {
                    advice.after(joinpoint);
                } catch (Throwable t) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("$Could not invoke joinpoint instance of target type '{}', \n"
                                + "  ClassLoader: {} \n"
                                + "  TargetMethod: {} \n"
                                + "  CurrentAdvice: {}", 
                                getTargetClass().getTypeName(),
                                targetClassLoader,
                                getAccessibleName(),
                                advice, 
                                t
                        );

                    Throwables.throwIfRequired(t);
                }
            }

            this.currentAdvice = null;
        }

        /**
         * {@inheritDoc}
         */
        /**
         * Returns the currently executing {@link Advice} instance, used by the joinpoint
         * to log the advice name in warning messages.
         *
         * @return the current advice, or {@code null} between advice invocations
         */
        @Override
        public Advice get() {
            return this.currentAdvice;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public Object getTargetObject() {
            return joinpoint.getTargetObject();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void setTargetObject(Object targetObject) {
            Descriptor descriptor = joinpoint.getDescriptor();
            if (dispatchBeforeAdvice && descriptor.isConstructor()) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Ignored 'this' reference assignment in constructor prior to initialization. \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  CurrentAdvice: {} \n",
                            descriptor.getTargetClass().getClassLoader(),
                            descriptor.getAccessibleName(),
                            currentAdvice
                    );
                }
                return;
            }

            joinpoint.setTargetObjectInternal(targetObject);
        }

        /**
         * {@inheritDoc}
         * <p>Returns the arguments from the underlying joinpoint, which may have been modified by before-advice.</p>
         */
        @Override
        public Object[] getArguments() {
            return joinpoint.getArguments();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void setArguments(Object[] arguments) {
            joinpoint.setArgumentsInternal(arguments);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public R getReturnValue() {
            return joinpoint.getReturnValue();
        }

        /**
         * {@inheritDoc}
         * <p>Delegates to the underlying joinpoint to record the target method's return value.</p>
         */
        @Override
        public void setReturnValue(R returnValue) {
            joinpoint.setReturnValueInternal(returnValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasAssignedReturnValue() {
            return joinpoint.hasAssignedReturnValue();
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public E getThrownException() {
            return joinpoint.getThrownException();
        }

        /**
         * {@inheritDoc}
         * <p>Delegates to the underlying joinpoint to record the target method's thrown exception.</p>
         */
        @Override
        public void setThrownException(E thrownException) {
            joinpoint.setThrownExceptionInternal(thrownException);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasAssignedThrownException() {
            return joinpoint.hasAssignedThrownException();
        }


        /**
         * Extends {@link MutableJoinpointDispatcher} to add per-type diagnostic logging
         * for joinpoint creation and before/after advice invocation.
         */
        static class Diagnostic<R, E extends Throwable> extends MutableJoinpointDispatcher<R, E> {

            private static final Logger LOGGER = LoggerFactory.getLogger(Diagnostic.class);


            public Diagnostic(Descriptor descriptor, 
                    Object targetObject, Object[] arguments, AopContext aopContext) {
                super(descriptor, targetObject, arguments, aopContext);
            }

            @Override
            protected void doInitialize(Descriptor descriptor, Object targetObject, Object[] arguments) {
                Class<?> targetClass = descriptor.getTargetClass();
                String targetTypeName = targetClass.getName();
                if (LOGGER.isInfoEnabled() && getAopContext().isDiagnosticType(targetTypeName)) {
                    LOGGER.info("^Creating joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n", 
                            targetTypeName, 
                            targetClass.getClassLoader(), 
                            descriptor.getAccessibleName()
                    );
                }

                super.doInitialize(descriptor, targetObject, arguments);
            }


            @Override
            protected void doInvokeBeforeAdvices(ClassLoader targetClassLoader) throws Throwable {
                Class<?> targetClass = getTargetClass();
                String targetTypeName = targetClass.getName();
                if (LOGGER.isInfoEnabled() && getAopContext().isDiagnosticType(targetTypeName))
                    LOGGER.info("^Invoking BeforeAdvices for joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  Advices: \n"
                            + "    {} \n", 
                            targetTypeName,
                            targetClassLoader, 
                            getAccessibleName(), 
                            StringUtils.join(getBeforeAdvices(), e -> e.getClass().getName(), "\n    ") 
                    );

                super.doInvokeBeforeAdvices(targetClassLoader);
            }

            @Override
            protected void doInvokeAfterAdvices(ClassLoader targetClassLoader) throws Throwable {
                Class<?> targetClass = getTargetClass();
                String targetTypeName = targetClass.getName();
                if (LOGGER.isInfoEnabled() && getAopContext().isDiagnosticType(targetTypeName))
                    LOGGER.info("^Invoking AfterAdvices for joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  Advices: \n"
                            + "    {} \n", 
                            targetTypeName,
                            targetClassLoader, 
                            getAccessibleName(), 
                            StringUtils.join(getAfterAdvices(), e -> e.getClass().getName(), "\n    ") 
                    );

                super.doInvokeAfterAdvices(targetClassLoader);
            }
        }
    }


    /**
     * Proceeding joinpoint passed to {@link io.gemini.api.aop.Advice.Around} advice.
     * Drives the around advice chain by calling each advice's {@code invoke} method in order,
     * and ultimately invokes the original target method via {@link #proceed()}.
     */
    class DefaultProceedingJoinpoint<R> extends AbstractBase<R, Throwable> implements ProceedingJoinpoint<R, Throwable> {

        private int currentAdviceIndex = 0;
        private final List<? extends Advice.Around<R, Throwable>> aroundAdvices;


        @SuppressWarnings("unchecked")
        public DefaultProceedingJoinpoint(Descriptor descriptor, 
                Object targetObject, Object[] arguments) {
            super(descriptor, targetObject, arguments);

            // initialize advisors
            List<? extends Advisor> advisors = descriptor.getAdvisors();
            advisors = CollectionUtils.isEmpty(advisors) ? Collections.emptyList() : advisors;

            List<Advice.Around<R, Throwable>> aroundAdvices = new ArrayList<>();
            for (Iterator<? extends Advisor> iterator = advisors.iterator(); iterator.hasNext(); ) {
                Advisor advisor = iterator.next();
                Class<? extends Advice> adviceClass = advisor.getAdviceClass();
                if (adviceClass == null) {
                    iterator.remove();

                    continue;
                }

                boolean isAroundAdvice = Advice.Around.class.isAssignableFrom(adviceClass);
                if (!isAroundAdvice) {
                    continue;
                }

                Advice.Around<R, Throwable> advice = (Advice.Around<R, Throwable>) advisor.getAdvice();
                if (advice == null) {
                    iterator.remove();

                    continue;
                }

                aroundAdvices.add(advice);
            }
            this.aroundAdvices = aroundAdvices;
        }

        @Override
        public R proceed() throws Throwable {
            if (currentAdviceIndex == this.aroundAdvices.size()) {
                try {
                    // TODO: super call
                    return null;
                } catch (Throwable t) {
                    if (t instanceof InterruptedException) 
                        Thread.currentThread().interrupt();

                    Throwables.propagate(t);
                }
            }

            return this.aroundAdvices.get(currentAdviceIndex++).invoke(this);
        }

        @Override
        public R proceed(Object... arguments) throws Throwable {
            super.setArgumentsInternal(arguments);

            return this.proceed();
        }
    }


    /**
     * Runtime dispatcher for around advice chains.
     * Created by the {@link io.gemini.aop.weaver.BootstrapDispatcher.Delegator} at each
     * instrumented joinpoint invocation and drives the {@link DefaultProceedingJoinpoint} chain.
     */
    @BootstrapClassConsumer
    class ProceedingJoinpointDispatcher<R> implements BootstrapDispatcher.Dispatcher<R, Throwable> {

        private static final Logger LOGGER = LoggerFactory.getLogger(ProceedingJoinpointDispatcher.class);

        private final AopContext aopContext;

        private DefaultProceedingJoinpoint<R> joinpoint = null;


        /**
         * Creates a new {@code ProceedingJoinpointDispatcher} for the given around-advice joinpoint.
         *
         * @param descriptor    the joinpoint descriptor
         * @param targetObject  the target object instance
         * @param arguments     the method arguments
         * @param aopContext    the central AOP context
         */
        public ProceedingJoinpointDispatcher(Descriptor descriptor, 
                Object targetObject, Object[] arguments, AopContext aopContext) {
            Assert.notNull(aopContext, "'aopContext' must not be null.");
            this.aopContext = aopContext;

            Class<?> targetClass = descriptor.getTargetClass();
            String targetTypeName = targetClass.getName();
            ClassLoader targetClassLoader = targetClass.getClassLoader();
            try {
                doInitialize(descriptor, targetObject, arguments);
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Could not create joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n", 
                            targetTypeName, 
                            targetClassLoader, 
                            descriptor.getAccessibleName(), 
                            t
                    );

                Throwables.throwIfRequired(t);
            }
        }

        protected void doInitialize(Descriptor descriptor, Object targetObject, Object[] arguments) {
            joinpoint = new DefaultProceedingJoinpoint<R>(descriptor, targetObject, arguments);
        }


        protected AopContext getAopContext() {
            return aopContext;
        }

        protected Class<?> getTargetClass() {
            return joinpoint.getDescriptor().getTargetClass();
        }

        protected Object getAccessibleName() {
            return joinpoint.getDescriptor().getAccessibleName();
        }

        protected List<? extends Around<R, Throwable>> getAroundAdvice() {
            return joinpoint.aroundAdvices;
        }


        /**
         * Dispatches the around advice chain by invoking {@link DefaultProceedingJoinpoint#proceed()}.
         * Sets the thread context class loader to the target class loader for the duration of the call.
         *
         * @return the result of the around advice chain
         * @throws Throwable if any advice or the target method throws
         */
        @Override
        public R dispatch() throws Throwable {
            String targetTypeName = getTargetClass().getName();
            ClassLoader targetClassLoader = getTargetClass().getClassLoader();
            try {
                return doDispatch();
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("$Could not proceed joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  Around advices: \n"
                            + "    {} \n", 
                            targetTypeName,
                            targetClassLoader,
                            getAccessibleName(), 
                            StringUtils.join(getAroundAdvice(), e -> e.getClass().getName(), "\n    "),
                            t
                    );

                // throw joinpoint exception
                Throwable rootCause = Throwables.unwrap(t);
                if (rootCause != null)
                    throw rootCause;

                // throw fatal error
                Throwables.throwIfRequired(t);
                return null;
            }
        }

        protected R doDispatch() throws Throwable {
            return joinpoint.proceed();
        }


        @Override
        public Object getTargetObject() {
            return joinpoint.getTargetObject();
        }

        /** 
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public void setTargetObject(Object targetObject) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         * <p>Returns the arguments from the underlying proceeding joinpoint.</p>
         */
        @Override
        public Object[] getArguments() {
            return joinpoint.getArguments();
        }

        /** 
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public void setArguments(Object[] arguments) {
            throw new UnsupportedOperationException();
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public R getReturnValue() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public void setReturnValue(R returnValue) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public boolean hasAssignedReturnValue() {
            throw new UnsupportedOperationException();
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public Throwable getThrownException() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public void setThrownException(Throwable thrownException) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         * <p>Not supported for around advice — throws {@link UnsupportedOperationException}.</p>
         */
        @Override
        public boolean hasAssignedThrownException() {
            throw new UnsupportedOperationException();
        }


        /**
         * Extends {@link ProceedingJoinpointDispatcher} to add per-type diagnostic logging
         * for joinpoint creation and around advice dispatch.
         */
        static class Diagnostic<R> extends ProceedingJoinpointDispatcher<R> {

            private static final Logger LOGGER = LoggerFactory.getLogger(Diagnostic.class);


            public Diagnostic(Descriptor descriptor, 
                    Object targetObject, Object[] arguments, AopContext aopContext) {
                super(descriptor, targetObject, arguments, aopContext);
            }


            @Override
            protected void doInitialize(Descriptor descriptor, Object targetObject, Object[] arguments) {
                Class<?> targetClass = descriptor.getTargetClass();
                String targetTypeName = targetClass.getName();
                if (LOGGER.isInfoEnabled() && getAopContext().isDiagnosticType(targetTypeName))
                    LOGGER.info("^Creating joinpoint instance of target type '{}', \n"
                            + "  ClassLoader: {} \n"
                            + "  TargetMethod: {} \n", 
                            targetTypeName, 
                            targetClass.getClassLoader(), 
                            descriptor.getAccessibleName()
                    );

                super.doInitialize(descriptor, targetObject, arguments);
            }


            @Override
            protected R doDispatch() throws Throwable {
                String targetTypeName = getTargetClass().getName();
                if (LOGGER.isInfoEnabled() && getAopContext().isDiagnosticType(targetTypeName))
                    LOGGER.info("^Proceeding joinpoint instance of target type '{}', \n"
                            + "  ClassTargetLoader: {} \n"
                            + "  TargetMethod: {} \n"
                            + "  Around advices: \n"
                            + "    {} \n", 
                            targetTypeName,
                            getTargetClass().getClassLoader(),
                            getAccessibleName(), 
                            StringUtils.join(getAroundAdvice(), e -> e.getClass().getName(), "\n    ")
                    );

                return super.doDispatch();
            }
        }
    }
}
