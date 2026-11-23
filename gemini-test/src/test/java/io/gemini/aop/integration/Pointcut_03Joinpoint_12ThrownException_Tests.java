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

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
public class Pointcut_03Joinpoint_12ThrownException_Tests {

    @Test
    public void testUpcastedType() {
        try {
            new ThrownException_Object().thrownException();
        } catch (Exception e) {
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ThrownException_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_AspectJ_01UpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownException_AspectJ_02UpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    public static class ThrownException_Object extends Exception {

        private static final long serialVersionUID = -8287385160059501926L;

        public static final String TARGET_METHOD = ThrownException_Object.class.getName() + ".thrownException";

        public void thrownException() throws ThrownException_Object {
            ThrownException_Object thrownException = new ThrownException_Object();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            throw thrownException;
        }
    }

    @Aspect
    public static class ThrownException_AspectJ_01UpcastedTypeBinding_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Pointcut_03Joinpoint_12ThrownException_Tests$ThrownException_Object"
                + ".thrownException(..) throws io.gemini.aop.integration.Pointcut_03Joinpoint_12ThrownException_Tests$ThrownException_Object)";


        private static final String AFTER_ADVICE = ThrownException_AspectJ_01UpcastedTypeBinding_Aspect.class.getName() + ".after";

        @AfterThrowing(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ThrownException_AspectJ_02UpcastedTypeBinding_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Pointcut_03Joinpoint_12ThrownException_Tests$ThrownException_Object"
                + ".thrownException(..) throws java.lang.Exception+)";


        @Pointcut(POINTCUT_EXPRESSION)
        private void thrownException() {
        }


        private static final String AFTER_ADVICE = ThrownException_AspectJ_02UpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }
}
