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

import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static org.assertj.core.api.Assertions.assertThat;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.MatchingContext;
import io.gemini.api.aop.Pointcut;
import io.gemini.api.aop.annotation.Conditional;
import io.gemini.api.aop.annotation.ConditionalOnClassLoader;
import io.gemini.api.aop.annotation.ConditionalOnField;
import io.gemini.api.aop.annotation.ConditionalOnMethod;
import io.gemini.api.aop.annotation.PojoPointcut;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Tests condition matching.
 *
 * @author   martin.liu
 */
public class Pointcut_02Condition_Tests {

    @Test
    public void testCondition() {
        new Target_Object().match();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Condition_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Condition_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class Target_Object {

        private void match() {
        }
    }

    static class Validation_Object {

        @SuppressWarnings("unused")
        private Validation_Object validationField; 

        public void validationMethod() {
        }
    }

    @PojoPointcut(Condition_Advisor.class)
    @ConditionalOnClassLoader(isAppClassLoader = true)
    @ConditionalOnMethod("* io.gemini.aop.integration.Pointcut_02Condition_Tests$Validation_Object.validationMethod()")
    public static class Condition_Advisor extends Advice.AbstractAfter<Void, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = Condition_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Void, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.Pointcut_02Condition_Tests$Target_Object");
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("match")
                    .and(isPrivate())
                    .and(returns(void.class));
        }
    }

    @Aspect
    @ConditionalOnClassLoader(classLoaderExpression = "AppClassLoader")
    @ConditionalOnValidationObject(verify = true)
    public static class Condition_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_02Condition_Tests$Target_Object.match())";

        private static final String AFTER_ADVICE = Condition_Aspect.class.getName() + ".after";


        @ConditionalOnField("io.gemini.aop.integration.Pointcut_02Condition_Tests$Validation_Object io.gemini.aop.integration.Pointcut_02Condition_Tests$Validation_Object.validationField")
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Void, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Conditional(OnValidationObjectCondition.class)
    @interface ConditionalOnValidationObject {

        boolean verify() default false;
    }


    public static class OnValidationObjectCondition implements ElementMatcher<MatchingContext> {

        private ConditionalOnValidationObject conditionalOnValidationObject;


        public OnValidationObjectCondition(ConditionalOnValidationObject conditionalOnValidationObject) {
            this.conditionalOnValidationObject = conditionalOnValidationObject;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(MatchingContext context) {
            return conditionalOnValidationObject.verify() ? context.hasType("io.gemini.aop.integration.Pointcut_02Condition_Tests$Validation_Object") : true;
        }
        
    }
}
