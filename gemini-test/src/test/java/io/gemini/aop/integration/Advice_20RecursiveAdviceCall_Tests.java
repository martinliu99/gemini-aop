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
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.annotation.EnableCircularityBreaker;
import io.gemini.api.aop.annotation.ExprPointcut;
import io.gemini.core.bootstrap.BootstrapClassConsumer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.OnNonDefaultValue;

/**
 * Tests recursive advice call.
 * 
 * @author   martin.liu
 */
public class Advice_20RecursiveAdviceCall_Tests {



    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object expected = new InstanceConstructor_Object(true);
expected.test(true);
//expected.test(true);
{
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_BeforeAdvice_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGETMETHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGETMETHOD = InstanceConstructor_Object.class.getName() + ".targetObject";

        public InstanceConstructor_Object(boolean targetObject) {}

        public void test(boolean targetObject) {
            System.out.println("entering InstanceConstructor_Object.test()");

            ExecutionMemento.putTargetMethodInvoker(TARGETMETHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );

            System.out.println("exiting InstanceConstructor_Object.t clest()");
        }
    }

    @ExprPointcut("execution(io.gemini.aop.integration.Advice_20RecursiveAdviceCall_Tests$InstanceConstructor_Object.new(boolean))")
    @EnableCircularityBreaker
    @BootstrapClassConsumer
    public static class InstanceConstructor_BeforeAdvice_Advisor {

        private static final String BEFORE_ADVICE = InstanceConstructor_BeforeAdvice_Advisor.class.getName() + ".before";

//        @OnMethodEnter(inline = false)
//        public static boolean before() {
////            if (!BootstrapDispatcher.isDispatchable()) {
////                return true;
////            } else
////                return false;
//
//            return false;
//        }

        @OnMethodExit(inline = false)
//        @Advice.AssignReturned.ToThis
        @EnableCircularityBreaker
        public static InstanceConstructor_Object after(
                @Advice.This InstanceConstructor_Object targetObject
                ) {
//            if (!BootstrapDispatcher.isDispatchable()) {
                System.out.println("Entering InstanceConstructor_Object.test_advice()");

                new InstanceConstructor_Object(false).test(false);;
//                try {
//                    methodHandle.invokeExact();
//                } catch (Throwable e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withTargetObject(null) );

//                return targetObject;
                System.out.println("Exiting InstanceConstructor_Object.test_advice()");
//            }
                return targetObject;
        }
    }

}
