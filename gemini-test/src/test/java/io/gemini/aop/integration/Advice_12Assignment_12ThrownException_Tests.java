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
/**
 * 
 */
package io.gemini.aop.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
@SuppressWarnings("unused")
public class Advice_12Assignment_12ThrownException_Tests {

    @Test
    public void testUpcastedType() {
        Throwable modified = null;
        try {
            new ThrownException_Object().thrownException();
        } catch (ThrownException_Object e) {
            modified = e;
        }

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ThrownException_Object.TARGET_METHOD)
                .getThrownException();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ThrownException_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_01Pojo_01UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_02AspectJ_01UpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_02AspectJ_02UpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_ByteBuddy_01UpcastedTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_ByteBuddy_02UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ThrownException_Object extends Exception {

        private static final long serialVersionUID = -8287385160059501926L;

        public static final String TARGET_METHOD = ThrownException_Object.class.getName() + ".thrownException";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_12ThrownException_Tests$ThrownException_Object.thrownException(..))";


        public void thrownException() throws ThrownException_Object {
            ThrownException_Object thrownException = new ThrownException_Object();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ThrownException_Object.POINTCUT_EXPRESSION)
    public static class ThrownException_01Pojo_01UpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ThrownException_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ThrownException_Object thrownException = new ThrownException_Object();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ThrownException_02AspectJ_01UpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ThrownException_02AspectJ_01UpcastedTypeBinding_Aspect.class.getName() + ".after";

        @AfterThrowing(value = ThrownException_Object.POINTCUT_EXPRESSION, argNames = "thrownException", throwing = "thrownException")
        public void after(Exception thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class ThrownException_02AspectJ_02UpcastedTypeBinding_RP_Aspect {

        @Pointcut(ThrownException_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ThrownException_02AspectJ_02UpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = ThrownException_Object.POINTCUT_EXPRESSION, argNames = "thrownException", throwing = "thrownException")
        public void after(Exception thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class ThrownException_02AspectJ_11IllegalUpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ThrownException_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.class.getName() + ".after";

        @AfterThrowing(value = ThrownException_Object.POINTCUT_EXPRESSION, argNames = "thrownException", throwing = "thrownException")
        public void after(RuntimeException thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class ThrownException_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect {

        @Pointcut(ThrownException_Object.POINTCUT_EXPRESSION)
        private void thrownException(RuntimeException thrownException) {
        }


        private static final String AFTER_ADVICE = ThrownException_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException(thrownException)", argNames = "thrownException", throwing = "thrownException")
        public void after(RuntimeException thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(ThrownException_Object.POINTCUT_EXPRESSION)
    public static class ThrownException_ByteBuddy_01UpcastedTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ThrownException_ByteBuddy_01UpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown Throwable thrownException
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(ThrownException_Object.POINTCUT_EXPRESSION)
    public static class ThrownException_ByteBuddy_02UpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ThrownException_ByteBuddy_02UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = ThrownException_Object.class)
        @Advice.AssignReturned.ToThrown
        public static ThrownException_Object after(
                @Advice.Thrown ThrownException_Object thrownException
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(ThrownException_Object.POINTCUT_EXPRESSION)
    public static class ThrownException_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor {

        private static final String BEFORE_ADVICE = ThrownException_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Return RuntimeException thrownException
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(ThrownException_Object.POINTCUT_EXPRESSION)
    public static class ThrownException_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ThrownException_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown
        public static long after(
                @Advice.Return ThrownException_Object thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return 1L;
        }
    }
}
