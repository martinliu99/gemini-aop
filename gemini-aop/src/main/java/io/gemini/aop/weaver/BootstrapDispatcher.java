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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import io.gemini.core.bootstrap.BootstrapClassProvider;

/**
 * This class is one of core classes of Gemini AOP framework used by {@link io.gemini.aop.weaver.advice.XxxAdvice}
 * to invoke {@code io.gemini.api.aop.Advice} instances at runtime.
 * 
 * <p>Class functionalities includes, 
 * <ol>
 *   <li>get or set {@code Delegator} instance.</li>
 *   <li>create INDY BSM method to get {@code CallSite}.</li>
 *   <li>break dispatcher invocation circularity.</li>
 * </ol>
 * 
 * 
 * <dl>
 * 
 * <dt>Class visibility.</dt>
 * <dd>To make instrumented target classes visible to this class, especially types under bootstrap 
 * ClassLoader, AOP framework annotates this classes with {@code @BootstrapClassProvider} annotation, 
 * to inject it into given package under bootstrap ClassLoader. The package name is defined by 
 * {@code @BootstrapClassProvider.scopeType}.
 *   </dd>
 * 
 * <dt>Get and cache joinpoint metadata</dt>
 * <dd>AOP framework uses {@code Descriptor} to hold joinpoint metadata, such as {@code Lookup} instance,
 * {@code AccessibleObject} instance, all matched {@code Advice} instances, and 
 * fetches {@code Descriptor} via {@code BootstrapDispatcher#callback(...)},
 * <ul>
 *   <li>Before JDK 7, return joinpoint {@code Descriptor} instance,
 *   <li>After JDK 7, return INDY BSM method holding joinpoint {@code Descriptor} instance.
 * </ul>
 * </dd>
 * 
 * <dt>Dispatcher circularity.</dt>
 * <dd>Bootstrap classes, such as java.lang.String, might be invoked in {@code BootstrapDispatcher} or
 * {@code Advice} which causes dispatching circularity. AOP provides {@code BootstrapDispatcher#enterDispatcher()}
 * method to disable {@code BootstrapDispatcher} and {@code BootstrapDispatcher#existDispatcher()} to
 * enable {@code BootstrapDispatcher}.
 * </dd> 
 * </dl>
 * 
 *
 * @author   martin.liu
 */
@BootstrapClassProvider( scopeType = String.class )
public abstract class BootstrapDispatcher {

    public static final String VAR_ADVICE_DISPATCHER = "_$$AdviceDispatcher$$";

    private static Delegator DELEGATOR;

    private static ThreadLocal<Boolean> IN_DISPATCHING = new ThreadLocal<>();


    private BootstrapDispatcher() {}


    /**
     * Initializes {@code BootstrapDispatcher#Delegator} field when AOP framework launches.
     * 
     * @param delegator instance of BootstrapDispatcher#Delegator
     */
    public static void setDelegator(Delegator delegator) {
        if (DELEGATOR != null) {
            System.err.println("BootstrapDispatcher.Delegator already initialized with " + DELEGATOR);
            return;
        }

        if (delegator == null) {
            throw new IllegalArgumentException("BootstrapDispatcher.Delegator must not be null.");
        }

        DELEGATOR = delegator;
    }

    /**
     * Get {@code BootstrapDispatcher#Delegator}.
     * 
     * @return initialized {@code BootstrapDispatcher#Delegator}.
     */
    public static Delegator getDelegator() {
        return DELEGATOR;
    }


    /**
     * Routes the INDY callback from instrumented bytecode to the registered
     * {@link Delegator}, which resolves the joinpoint {@code Descriptor} for framework
     * managed {@code io.gemini.api.aop.Advice}, or {@code MethodHandle} for Byte Buddy 
     * {@code net.bytebuddy.asm.Advice}. 
     * The return value could be regular java object, or a {@link java.lang.invoke.CallSite} 
     * that holds the object.
     *
     * @param targetLookup the lookup context of the instrumented class
     * @param methodName   the bootstrap method name
     * @param methodType   the method type of the call site
     * @param arguments    additional bootstrap arguments
     * @return the result from the delegator callback
     */
    public static Object callback(MethodHandles.Lookup targetLookup, 
            String methodName, MethodType methodType, Object... arguments) {
        return DELEGATOR.callback(targetLookup, methodName, methodType, arguments);
    }


    /**
     * Returns whether the dispatcher is currently able to dispatch advice calls.
     * Returns {@code false} if a dispatch is already in progress on this thread,
     * preventing re-entrant (circular) dispatch.
     *
     * @return {@code true} if dispatching is allowed on the current thread
     */
    public static boolean isDispatchable() {
        return IN_DISPATCHING.get() == null;
    }

    /**
     * Re-enables dispatching on the current thread after a dispatch cycle completes.
     * Should be called in a {@code finally} block paired with {@link #disableDispatch()}.
     */
    public static void enableDispatch() {
        IN_DISPATCHING.remove();
    }

    /**
     * Disables dispatching on the current thread to prevent re-entrant advice invocation.
     * Must be paired with a subsequent call to {@link #enableDispatch()}.
     */
    public static void disableDispatch() {
        IN_DISPATCHING.set( Boolean.TRUE );
    }


    /**
     * This class provides below functionalities,
     * 
     * <ol>
     * <li>notify {@code Advice} instances before target method invocation,
     * and after target method invocation with return value or thrown exception of target method,
     * <li>modify arguments of target method. 
     * </ol>
     * 
     */
    @BootstrapClassProvider( scopeType = String.class )
    public static interface Dispatcher<T, E extends Throwable> {

        /**
         * Dispatches the before or after advice chain for the current joinpoint.
         * Returns {@code null} for before/after advice; may return a value for around advice.
         *
         * @return the advice result, or {@code null}
         * @throws E if any advice throws
         */
        T dispatch() throws E;


        /**
         * Returns the object that holds the current joinpoint's static part.
         *
         * <p>For instance, the target object for an invocation.
         *
         * @return the object (can be null if the accessible object is
         * static). */
        Object getTargetObject();

        /**
         * Sets the object that holds the current joinpoint's static part.
         * 
         * @param targetObject the target object
         */
        void setTargetObject(Object targetObject);


        /**
         * Returns the current method arguments, which may have been modified by before-advice.
         *
         * @return the method arguments array
         */
        Object[] getArguments();

        /**
         * Set the current method arguments.
         * 
         * @param arguments method arguments
         */
        void setArguments(Object[] arguments);


        /**
         * Returns the return value.
         *
         * @return the return value
         */
        T getReturnValue();

        /**
         * Initializes the actual return value from the target method invocation.
         *
         * @param returnValue the return value
         */
        void setReturnValue(T returnValue);

        /**
         * Returns whether any advice has set an return value.
         *
         * @return {@code true} if a return value is present
         */
        boolean hasAssignedReturnValue();


        /**
         * Returns the thrown exception.
         *
         * @return the thrown exception
         */
        E getThrownException();

        /**
         * Initializes the actual exception thrown by the target method.
         *
         * @param thrownException the thrown exception, or {@code null}
         */
        void setThrownException(E thrownException);

        /**
         * Returns whether any advice has set an exception.
         *
         * @return {@code true} if an exception is present
         */
        boolean hasAssignedThrownException();
    }


    /**
     * This interface defines APIs to actually process request from instrumented bytecode.
     *
     */
    @BootstrapClassProvider( scopeType = String.class )
    public static interface Delegator {

        /**
         * Processes the callback from instrumented bytecode.
         * The return value could be regular java object, or a {@link java.lang.invoke.CallSite} 
         * that holds the object.
         *
         * @param targetLookup the lookup context of the instrumented class
         * @param methodName   the bootstrap method name
         * @param methodType   the method type of the call site
         * @param arguments         additional bootstrap arguments
         * @return the java object or a {@link java.lang.invoke.CallSite}, or {@code null}
         */
        Object callback(MethodHandles.Lookup targetLookup, String methodName, 
                MethodType methodType, Object... arguments);


        /**
         * Creates a {@link Dispatcher} instance for the given joinpoint, wiring the
         * descriptor, target object, and arguments for the current invocation.
         *
         * @param <T>          the return type of the target method
         * @param <E>          the exception type declared by the target method
         * @param descriptor   the joinpoint descriptor (as {@link Object} to avoid bootstrap class dependency)
         * @param targetObject the target object instance, or {@code null} for static methods
         * @param arguments    the method arguments
         * @return a new {@link Dispatcher} for this invocation
         */
        <T, E extends Throwable> Dispatcher<T, E> createDispacther(
                Object descriptor, Object targetObject, Object[] arguments);
    }
}
