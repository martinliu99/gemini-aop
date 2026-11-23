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


import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.isPackagePrivate;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static org.assertj.core.api.Assertions.assertThat;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.Pointcut;
import io.gemini.api.aop.annotation.ExprPointcut;
import io.gemini.api.aop.annotation.PojoPointcut;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Tests advice class visibility.
 *
 * @author   martin.liu
 */
public class Advice_03AdviceVisibility_Tests {

    private static final String POINTCUT_EXPRESSION = "execution(!private long io.gemini.aop.integration.Advice_03AdviceVisibility_Tests$AdviceVisibility_Object.match(long))";


    @Test
    public void testAdviceVisibility() {
        new AdviceVisibility_Object().match(1l);

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_PojoPointcut_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_ExprPointcut_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_InvisibleAspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_ByteBuddy_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker2 = ExecutionMemento.getAdviceMethodInvoker(AdviceVisibility_ByteBuddy_InvisibleAdvisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker2).isNull();
        }
    }

    public static class AdviceVisibility_Object {

        long match(long input) {
            return input;
        }
    }

    @SuppressWarnings("rawtypes")
    @PojoPointcut(AdviceVisibility_PojoPointcut_Advisor.class)
    private static class AdviceVisibility_PojoPointcut_Advisor extends Advice.AbstractAfter implements Pointcut {

        private static final String AFTER_ADVICE = AdviceVisibility_PojoPointcut_Advisor.class.getName() + ".after";

        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }

        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.Advice_03AdviceVisibility_Tests$AdviceVisibility_Object");
        }

        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("match")
                    .and(isPackagePrivate())
                    .and(takesArgument(0, is(long.class)))
                    .and(returns(long.class));
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(POINTCUT_EXPRESSION)
    private static class AdviceVisibility_ExprPointcut_Advisor extends Advice.AbstractAfter {

        private static final String AFTER_ADVICE = AdviceVisibility_ExprPointcut_Advisor.class.getName() + ".after";

        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class AdviceVisibility_Aspect {

        private static final String AFTER_ADVICE = AdviceVisibility_Aspect.class.getName() + ".after";

        @After(POINTCUT_EXPRESSION)
        void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class AdviceVisibility_InvisibleAspect {

        private static final String AFTER_ADVICE = AdviceVisibility_InvisibleAspect.class.getName() + ".after";

        @After(POINTCUT_EXPRESSION)
        private void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    private static class AdviceVisibility_ByteBuddy_Advisor {

        private static final String AFTER_ADVICE = AdviceVisibility_ByteBuddy_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        static void after(
                @net.bytebuddy.asm.Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    private static class AdviceVisibility_ByteBuddy_InvisibleAdvisor {

        private static final String AFTER_ADVICE = AdviceVisibility_ByteBuddy_InvisibleAdvisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        private static void after(
                @net.bytebuddy.asm.Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }
}