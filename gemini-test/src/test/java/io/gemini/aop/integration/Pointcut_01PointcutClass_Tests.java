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
package io.gemini.aop.integration;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.Pointcut;
import io.gemini.api.aop.annotation.ExprPointcut;
import io.gemini.api.aop.annotation.PojoPointcut;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Tests advice, e.g., Gemini advice, AspectJ adivce, or ByteBuddy advice, etc.
 *
 * @author   martin.liu
 */
public class Pointcut_01PointcutClass_Tests {

    private static final String POINTCUT_EXPRESSION = "execution(!private void io.gemini.aop.integration.Pointcut_01PointcutClass_Tests$PointcutClass_Object.match())";


    @Test
    public void testPointcutClass() {
        new PointcutClass_Object().match();

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_01Pojo_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_02Pojo_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_AspectJ_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_AspectJ_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_01ByteBuddy_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PointcutClass_02ByteBuddy_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    private static class PointcutClass_Object {

        void match() { }
    }

    @SuppressWarnings("rawtypes")
    @PojoPointcut(PointcutClass_01Pojo_Advisor.class)
    private static class PointcutClass_01Pojo_Advisor extends Advice.AbstractBefore implements Pointcut {

        private static final String BEFORE_ADVICE = PointcutClass_01Pojo_Advisor.class.getName() + ".before";

        /**
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.Pointcut_01PointcutClass_Tests$PointcutClass_Object");
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("match");
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Pointcut_01PointcutClass_Tests.POINTCUT_EXPRESSION)
    private static class PointcutClass_02Pojo_Advisor extends Advice.AbstractAfter {

        private static final String AFTER_ADVICE = PointcutClass_02Pojo_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    protected static class PointcutClass_AspectJ_Aspect {

        private static final String BEFORE_ADVICE = PointcutClass_AspectJ_Aspect.class.getName() + ".before";

        @Before(Pointcut_01PointcutClass_Tests.POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }


        private static final String AFTER_ADVICE = PointcutClass_AspectJ_Aspect.class.getName() + ".after";

        @AfterReturning(pointcut = Pointcut_01PointcutClass_Tests.POINTCUT_EXPRESSION, returning = "returnValue")
        public static void after(Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(returnValue) );
        }
    }

    @PojoPointcut(PointcutClass_01ByteBuddy_Advisor.class)
    static class PointcutClass_01ByteBuddy_Advisor implements Pointcut {

        private static final String BEFORE_ADVICE = PointcutClass_01ByteBuddy_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.Pointcut_01PointcutClass_Tests$PointcutClass_Object");
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("match");
        }
    }

    @ExprPointcut(Pointcut_01PointcutClass_Tests.POINTCUT_EXPRESSION)
    static class PointcutClass_02ByteBuddy_Advisor {

        private static final String AFTER_ADVICE = PointcutClass_02ByteBuddy_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @net.bytebuddy.asm.Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }
}
