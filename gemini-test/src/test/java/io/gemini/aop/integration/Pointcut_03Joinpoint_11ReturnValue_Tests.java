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

import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests.TargetObject_Base_Object;
import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
public class Pointcut_03Joinpoint_11ReturnValue_Tests {

    @Test
    public void testUpcastedType() {
        new ReturnValue_Super_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_Super_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01UpcastedTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_02UpcastedTypeMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    public static class ReturnValue_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public ReturnValue_Super_Object returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_Super_Object "
                + "io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_Super_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01UpcastedTypeMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_02UpcastedTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests.TargetObject_Base_Object+ "
                + "io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_Super_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_02UpcastedTypeMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testVoidType() {
        ReturnValue_VoidType_Object object = new ReturnValue_VoidType_Object();
        object.returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_VoidType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01VoidTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalVoidTypeMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_VoidType_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public void returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01VoidTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_VoidType_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01VoidTypeMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalVoidTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Void io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_VoidType_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalVoidTypeMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testBoxedPrimitive() {
        new ReturnValue_BoxedPrimitive_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_BoxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01BoxedPrimitiveMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalBoxedPrimitiveMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_BoxedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public int returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return 1;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01BoxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public int io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_BoxedPrimitive_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01BoxedPrimitiveMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalBoxedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Integer io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_BoxedPrimitive_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalBoxedPrimitiveMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testUnboxedPrimitive() {
        new ReturnValue_UnboxedPrimitive_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_UnboxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01UnboxedPrimitiveMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalUnboxedPrimitiveMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_UnboxedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public Integer returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return 1;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01UnboxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Integer io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_UnboxedPrimitive_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01UnboxedPrimitiveMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalUnboxedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public int io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_UnboxedPrimitive_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalUnboxedPrimitiveMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testWidenedPrimitive() {
        new ReturnValue_WidenedPrimitive_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_WidenedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01WidenedPrimitiveMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalWidenedPrimitiveMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_WidenedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public int returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return 1;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01WidenedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public int io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_WidenedPrimitive_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01WidenedPrimitiveMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalWidenedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public long io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_WidenedPrimitive_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalWidenedPrimitiveMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericType() {
        new ReturnValue_GenericType_Object<>().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01GenericTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalGenericTypeMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_GenericType_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = ReturnValue_GenericType_Object.class.getName() + ".returnValue";


        public T returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01GenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Number io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_GenericType_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01BoxedPrimitiveMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalGenericTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public T io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_GenericType_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalBoxedPrimitiveMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericArray() {
        new ReturnValue_GenericArray_Object<>().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericArray_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01GenericArrayMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalGenericArrayMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_GenericArray_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = ReturnValue_GenericArray_Object.class.getName() + ".returnValue";


        public T[][] returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01GenericArrayMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Number[][] io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_GenericArray_Object.returnValue(..))";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01BoxedPrimitiveMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalGenericArrayMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public T[][] io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_GenericArray_Object.returnValue(..))";


        @Pointcut(POINTCUT_EXPRESSION)
        public void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalBoxedPrimitiveMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testParameterizedCollection() {
        new ReturnValue_ParameterizedCollectionReturnType_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_ParameterizedCollectionReturnType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_02ParameterizedCollectionMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalParameterizedCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }


    static class ReturnValue_ParameterizedCollectionReturnType_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        List<Integer> returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<java.lang.Integer> io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_ParameterizedCollectionReturnType_Object"
                + ".returnValue())";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_02ParameterizedCollectionMatching_RP_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_ParameterizedCollectionReturnType_Object"
                + ".returnValue())";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_02ParameterizedCollectionMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<java.lang.String> io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_ParameterizedCollectionReturnType_Object"
                + ".returnValue())";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalParameterizedCollectionMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testWildCardCollection() {
        new ReturnValue_WildCardCollectionReturnType_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_WildCardCollectionReturnType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01WildCardCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_02WildCardCollectionMatching_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalWildCardCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }


    static class ReturnValue_WildCardCollectionReturnType_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        List<? super Number> returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<? super java.lang.Number> io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_WildCardCollectionReturnType_Object"
                + ".returnValue())";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01WildCardCollectionMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_02WildCardCollectionMatching_RP_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_WildCardCollectionReturnType_Object"
                + ".returnValue())";


        @Pointcut(POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_02WildCardCollectionMatching_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()")
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalWildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<java.lang.Number> io.gemini.aop.integration.Pointcut_03Joinpoint_11ReturnValue_Tests$ReturnValue_WildCardCollectionReturnType_Object"
                + ".returnValue())";


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalWildCardCollectionMatching_Aspect.class.getName() + ".after";

        @AfterReturning(value = POINTCUT_EXPRESSION)
        public void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }
}
