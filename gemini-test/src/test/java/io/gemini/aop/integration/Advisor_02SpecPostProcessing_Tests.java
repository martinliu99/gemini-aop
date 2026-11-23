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

import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.MatchingContext;
import io.gemini.api.aop.annotation.Conditional;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Tests post processing of advice spec scanning.
 *
 * @author   martin.liu
 */
public class Advisor_02SpecPostProcessing_Tests {

    @Test
    public void testSpecScanning() {
        new SpecPostPrcoessing_Object().postPrcoessSpec(1l);

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(SpecPostProcessing_ExprPointcut_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class SpecPostPrcoessing_Object {

        public long postPrcoessSpec(long input) {
            return input;
        }
    }

    static class SpecPostProcessing_ExprPointcut_Advisor extends Advice.AbstractAfter<Long, RuntimeException> {

        private static final String AFTER_ADVICE = SpecPostProcessing_ExprPointcut_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    static class Validation_Object {

        @SuppressWarnings("unused")
        private Validation_Object validationField; 

        public void validationMethod() {
        }
    }

    @Conditional(OnValidationObjectCondition.class)
    @interface ConditionalOnValidationObject {

        boolean verify() default false;
    }


    static class OnValidationObjectCondition implements ElementMatcher<MatchingContext> {

        private boolean verify;

        public OnValidationObjectCondition(boolean verify) {
            this.verify = verify;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(MatchingContext context) {
            return verify ? context.isAppClassLoader() && context.hasType("io.gemini.aop.integration.Pointcut_02ConditionMatching_Tests$ConditionMatching_Object") : true;
        }
        
    }
}
