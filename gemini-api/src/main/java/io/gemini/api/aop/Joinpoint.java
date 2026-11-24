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
package io.gemini.api.aop;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;

/**
 * Represents the runtime context of a method, constructor or type initializer invocation 
 * intercepted by the Gemini AOP framework.
 * 
 * <p>
 * Provides access to the target object, its class, the intercepted method/constructor,
 * and the invocation arguments. 
 * Shares additional contextual key value pair across one method invocation.
 * </p>
 *
 * @author   martin.liu
 */
public interface Joinpoint {

    /**
     * Returns Lookup of target class to access members of target object
     * @return lookup of target class
     */
    Lookup getTargetLookup();

    /**
     * Returns target class
     * @return target class
     */
    Class<?> getTargetClass();

    /**
     * Returns the static part of this joinpoint.
     *
     * <p>The static part is an accessible object on which a chain of
     * advices are installed. */
    AccessibleObject getStaticPart();


    /**
     * Returns the object that holds the current joinpoint's static part.
     *
     * <p>For instance, the target object for an invocation.
     *
     * @return the object (can be null if the accessible object is
     * static). */
    Object getTargetObject();

    /**
     * Get the arguments as an array object.
     * It is impossible to change element values within this
     * array to change the arguments.
     *
     * @return arguments of the invocation */
    Object[] getArguments();


    /**
     * Returns attached additional context value for provided key.
     * 
     * @param key   the key of context value
     * @return      context value
     */
    Object getInvocationContext(String key);

    /**
     * Attaches additional contextual key value pair across one method invocation.
     * 
     * @param key   the key of context value
     * @param value context value
     */
    void setInvocationContext(String key, Object value);


    /**
     * The {@link MutableJoinpoint} sub-interface additionally exposes the 
     * return value and thrown exception, and allows advice to override them. 
     *
     * @param <R>   return value
     * @param <E>   thrown exception
     */
    interface MutableJoinpoint<R, E extends Throwable> extends Joinpoint {

        /**
         * Sets the target object that advice wants to substitute for the target method's owner object.
         * 
         * @param targetObject the target object
         */
        void setTargetObject(Object targetObject);

        /**
         * Sets arguments that advice wants to substitute for the target method's arguments.
         * 
         * @param arguments arguments of the invocation
         */
        void setArguments(Object... arguments);


        /**
         * Returns the return value of target method invocation, or throw IllegalStateException when called 
         * in {@link io.gemini.api.aop.Advice.Before} advice.
         * 
         * @return      the return value
         * @throws IllegalStateException    thrown exception when called in {@link io.gemini.api.aop.Advice.Before} advice
         */
        R getReturnValue() throws IllegalStateException;

        /**
         * Sets the return value that advice wants to substitute for the target method's result.
         * 
         * @param returnValue     the return value
         */
        void setReturnValue(R returnValue);


        /**
         * Returns the thrown exception of target method invocation, or throw IllegalStateException when called 
         * in {@link io.gemini.api.aop.Advice.Before} advice.
         * 
         * @return the thrown exception
         * @throws IllegalStateException    thrown exception when called in {@link io.gemini.api.aop.Advice.Before} advice.
         */
        E getThrownException() throws IllegalStateException;

        /**
         * Sets the exception that advice wants to substitute for the target method's thrown exception.
         *
         * @param thrownException the thrown exception
         */
        void setThrownException(E thrownException);
    }


    /**
     * The {@link ProceedingJoinpoint} sub-interface additionally exposes the proceed(..) 
     * method to support {@link io.gemini.api.aop.Advice.Around} advice. 
     * 
     * TODO: not supported
     * 
     * @param <R>   return value
     * @param <E>   thrown exception
     */
    interface ProceedingJoinpoint<R, E extends Throwable> extends Joinpoint {

        /**
         * Proceeds to the next advice in the chain or target method invocation.
         *
         * @return the return value
         */
        R proceed() throws E;

        /**
         * Proceeds to the next advice in the chain or target method invocation 
         * with overrode arguments.
         * 
         * @param arguments     the arguments of method invocation
         * @return              the return value of method invocation
         * @throws Throwable    the thrown exception
         */
        R proceed(Object... arguments) throws E;
    }
}
