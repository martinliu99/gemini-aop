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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;

/**
 * Tests matching of target type referred by advice method, e.g, static and dynamic type matching,
 * raw and generic type matching etc.
 * 
 * @author   martin.liu
 */
public class Pointcut_03Joinpoint_02TargetObject_Tests {

    @Test
    public void testUpcastedType() {
        {
            TargetObject_Super_Object object = new TargetObject_Super_Object();
            object.targetObject();

            {
                TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TargetObject_Super_Object.TARGET_METHOD);
                assertThat(targetMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_01UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_02UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_03UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_04UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }
        }


        ExecutionMemento.clearMemento();

        {
            TargetObject_Sub_Object object = new TargetObject_Sub_Object();
            object.targetObject();

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_01UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_02UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_03UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_04UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNotNull();
            }

            {
                AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_11UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
                assertThat(beforeAdviceMethodInvoker).isNull();
            }
        }
    }

    public static abstract class TargetObject_Base_Object { }

    public static class TargetObject_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = TargetObject_Super_Object.class.getName() + ".targetObject";

        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    public static class TargetObject_Sub_Object extends TargetObject_Super_Object { }

    @Aspect
    public static class TargetObject_AspectJ_01UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Super_Object.targetObject(..))";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_01UpcastedTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_02UpcastedTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "within(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests) "
                + "&& execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Base_Object+.targetObject(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        public void targetObject() {  }

        private static final String BEFORE_ADVICE = TargetObject_AspectJ_02UpcastedTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_03UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "within(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests) "
                + "&& target(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Base_Object) "
                + "&& execution(public void targetObject())";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_03UpcastedTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_04UpcastedTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "within(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests) "
                + "&& target(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Super_Object) "
                + "&& execution(* targetObject(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        public void targetObject() {  }

        private static final String BEFORE_ADVICE = TargetObject_AspectJ_04UpcastedTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_11UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "within(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests) "
                + "&& target(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Sub_Object) "
                + "&& execution(public void targetObject())";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_11UpcastedTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericType() {
        TargetObject_GenericType_Object<Integer> object = new TargetObject_GenericType_Object<>();
        object.targetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TargetObject_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_01GenericTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_02GenericTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_03GenericTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_11IllegalGenericTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_AspectJ_12IllegalGenericTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class TargetObject_GenericType_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = TargetObject_GenericType_Object.class.getName() + ".targetObject";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_01GenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object.targetObject(..)) ";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_01GenericTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_02GenericTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "within(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests) "
                + "&& execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Base_Object+.targetObject(..)) ";


        @Pointcut(POINTCUT_EXPRESSION)
        public void targetObject() {  }

        private static final String BEFORE_ADVICE = TargetObject_AspectJ_02GenericTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_03GenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object.targetObject(..)) "
                + "&& target(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object) ";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_03GenericTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_11IllegalGenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object<T>.targetObject(..)) ";


        private static final String BEFORE_ADVICE = TargetObject_AspectJ_11IllegalGenericTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class TargetObject_AspectJ_12IllegalGenericTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object<T>.targetObject(..)) "
                + "&& target(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_GenericType_Object<T>) ";

        @Pointcut(POINTCUT_EXPRESSION)
        public void targetObject() {  }

        private static final String BEFORE_ADVICE = TargetObject_AspectJ_12IllegalGenericTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }
}
