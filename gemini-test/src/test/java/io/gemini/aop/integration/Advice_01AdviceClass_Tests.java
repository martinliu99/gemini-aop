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

import static org.assertj.core.api.Assertions.assertThat;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Tests advice, e.g., Gemini advice, AspectJ adivce, or ByteBuddy advice, etc.
 *
 * @author   martin.liu
 */
public class Advice_01AdviceClass_Tests {

    private static final String POINTCUT_EXPRESSION = "execution(!private void io.gemini.aop.integration.Advice_01AdviceClass_Tests$AdviceClass_Object.match())";


    @Test
    public void testAdviceClass() {
        new AdviceClass_Object().match();

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_Pojo_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_Pojo_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_ByteBuddy_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_ByteBuddy_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_AspectJ_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceClass_AspectJ_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    private static class AdviceClass_Object {

        void match() { }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Advice_01AdviceClass_Tests.POINTCUT_EXPRESSION)
    private static class AdviceClass_Pojo_Advisor extends Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = AdviceClass_Pojo_Advisor.class.getName() + ".before";

        /**
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }


        private static final String AFTER_ADVICE = AdviceClass_Pojo_Advisor.class.getName() + ".after";

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
    protected static class AdviceClass_AspectJ_Aspect {

        private static final String BEFORE_ADVICE = AdviceClass_AspectJ_Aspect.class.getName() + ".before";

        @Before(Advice_01AdviceClass_Tests.POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }


        private static final String AFTER_ADVICE = AdviceClass_AspectJ_Aspect.class.getName() + ".after";

        @AfterReturning(pointcut = Advice_01AdviceClass_Tests.POINTCUT_EXPRESSION, returning = "returnValue")
        public static void after(Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(returnValue) );
        }
    }

    @ExprPointcut(Advice_01AdviceClass_Tests.POINTCUT_EXPRESSION)
    static class AdviceClass_ByteBuddy_Advisor {

        private static final String BEFORE_ADVICE = AdviceClass_ByteBuddy_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }


        private static final String AFTER_ADVICE = AdviceClass_ByteBuddy_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @net.bytebuddy.asm.Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }
}
