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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.Joinpoint.ProceedingJoinpoint;

/**
 * Defines the core advice interfaces and abstract base classes for the Gemini AOP framework.
 * <p>
 * Advice implementations intercept method executions at joinpoints. Three advice styles are supported:
 * <ul>
 *   <li>{@link Before} – executed before the target method</li>
 *   <li>{@link After} – executed after the target method (regardless of outcome)</li>
 *   <li>{@link Around} – wraps the target method, controlling invocation via {@link io.gemini.api.aop.Joinpoint.ProceedingJoinpoint#proceed()}</li>
 * </ul>
 * </p>
 * <p>
 * Concrete advice classes could extend one of the provided abstract base classes
 * ({@link AbstractBefore}, {@link AbstractAfter}, {@link AbstractBeforeAfter}) for convenience.
 * </p>
 *
 * @author   martin.liu
 */
public interface Advice {

    /**
     * Advice executed before the target method invocation.
     *
     * @param <R> the return type of the target method
     * @param <E> the checked exception type declared by the target method
     */
    interface Before<R, E extends Throwable> extends Advice {

        /**
         * Called before the target method. 
         * <ul>
         * <li>Arguments can be modified via {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#getArguments()}.
         * <li>The return value or thrown exception can be set
         * via {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#setReturnValue(Object)} or
         * {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#setThrownException(Throwable)} to bypass target
         * method invocation.
         * </ul>
         *
         * @param joinpoint the mutable joinpoint providing access to target object, arguments, and result
         * @throws Throwable if the advice itself throws an exception
         */
        void before(MutableJoinpoint<R, E> joinpoint) throws Throwable;

    }

    /**
     * Advice executed after the target method invocation.
     *
     * @param <R> the return type of the target method
     * @param <E> the checked exception type declared by the target method
     */
    interface After<R, E extends Throwable> extends Advice {

        /**
         * Called after the target method.
         * <ul>
         * <li>Arguments can be modified via {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#getArguments()}.
         * <li>The return value or thrown exception can be set
         * via {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#setReturnValue(Object)} or
         * {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#setThrownException(Throwable)} to 
         * override target method invocation.
         * <li>The return value or thrown exception can be inspected
         * via {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#getReturnValue()} and
         * {@link io.gemini.api.aop.Joinpoint.MutableJoinpoint#getThrownException()}.
         * </ul>
         *
         * @param joinpoint the mutable joinpoint
         * @throws Throwable if the advice itself throws an exception
         */
        void after(MutableJoinpoint<R, E> joinpoint) throws Throwable;

    }

    /**
     * Around advice that fully controls the target method invocation.
     * 
     * TOOD: not supported
     *
     * @param <R> the return type of the target method
     * @param <E> the checked exception type declared by the target method
     */
    interface Around<R, E extends Throwable> extends Advice {

        /**
         * Wraps the target method. Call {@link io.gemini.api.aop.Joinpoint.ProceedingJoinpoint#proceed()}
         * to invoke the original method.
         *
         * @param joinpoint the proceeding joinpoint
         * @return the return value to use (may differ from the original)
         * @throws E if the target method or advice throws
         */
        R invoke(ProceedingJoinpoint<R, E> joinpoint) throws E;

    }


    /**
     * Defines simple abstract base class for interface {@link Advice}.
     */
    abstract class AbstractBase implements Advice {

        protected static final Logger LOGGER = LoggerFactory.getLogger(Advice.class);
    }


    /**
     * Defines simple abstract base class for interface {@link Before}.
     */
    abstract class AbstractBefore<R, E extends Throwable> extends AbstractBase implements Before<R, E> {

    }


    /**
     * Defines simple abstract base class for interface {@link After}.
     */
    abstract class AbstractAfter<R, E extends Throwable> extends AbstractBase implements After<R, E> {
    }


    /**
     * Defines simple abstract base class for interface {@link Before} and {@link After}.
     */
    abstract class AbstractBeforeAfter<R, E extends Throwable> extends AbstractBase implements Before<R, E>, After<R, E> {

    }
}