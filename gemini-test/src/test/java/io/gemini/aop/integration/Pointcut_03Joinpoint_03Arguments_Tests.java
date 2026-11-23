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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests.TargetObject_Base_Object;
import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;

/**
 * Tests matching of target type referred by advice method, e.g, static and dynamic type matching,
 * raw and generic type matching etc.
 * 
 * @author   martin.liu
 */
public class Pointcut_03Joinpoint_03Arguments_Tests {

    @Test
    public void testUpcastedType() {
        Argument_Super_Object object = new Argument_Super_Object();
        Argument_Super_Object arg = new Argument_Super_Object();
        object.arguments(arg);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_Super_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_03UpcastedTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_04UpcastedTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }
    }

    public static class Argument_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_Super_Object.class.getName() + ".arguments";

        public void arguments(Argument_Super_Object arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object"
                + ".arguments(io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01UpcastedTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02UpcastedTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object"
                + ".arguments(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Base_Object+)) ";


        @Pointcut(POINTCUT_EXPRESSION)
        private void arguments() {
        }


        private static final String BEFORE_ADVICE = Arguments_AspectJ_02UpcastedTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_03UpcastedTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object.arguments(..)) "
                + "&& args(io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_03UpcastedTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = Arguments_AspectJ_01UpcastedTypeMatching_Aspect.POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_04UpcastedTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_Super_Object.arguments(..)) "
                + "&& args(io.gemini.aop.integration.Pointcut_03Joinpoint_02TargetObject_Tests$TargetObject_Base_Object)";


        @Pointcut(Arguments_AspectJ_01UpcastedTypeMatching_Aspect.POINTCUT_EXPRESSION)
        private void arguments() {
        }


        private static final String BEFORE_ADVICE = Arguments_AspectJ_04UpcastedTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testBoxedPrimitive() {
        Argument_BoxedPrimitive_Object object = new Argument_BoxedPrimitive_Object();
        int arg = 1;
        object.arguments(arg);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_BoxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01BoxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02BoxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_03BoxedPrimitiveMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_04IllegalBoxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_BoxedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_BoxedPrimitive_Object.class.getName() + ".arguments";

        public void arguments(int arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01BoxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_BoxedPrimitive_Object"
                + ".arguments(int)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01BoxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02BoxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_BoxedPrimitive_Object.arguments(..)) "
                + "&& args(java.lang.Integer)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_02BoxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_03BoxedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_BoxedPrimitive_Object.arguments(..)) "
                + "&& args(java.lang.Object)";


        @Pointcut(POINTCUT_EXPRESSION)
        private void arguments() { }

        private static final String BEFORE_ADVICE = Arguments_AspectJ_03BoxedPrimitiveMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_04IllegalBoxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_BoxedPrimitive_Object"
                + ".arguments(java.lang.Integer)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_04IllegalBoxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test 
    public void testUnboxedPrimitive() {
        Argument_UnboxedPrimitive_Object object = new Argument_UnboxedPrimitive_Object();
        Integer arg = 1;
        object.arguments(arg);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_UnboxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01UnboxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02UnboxedPrimitiveMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_03UnboxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalUnboxedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_UnboxedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_UnboxedPrimitive_Object.class.getName() + ".arguments";

        public void arguments(Integer arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01UnboxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_UnboxedPrimitive_Object"
                + ".arguments(java.lang.Integer)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01UnboxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02UnboxedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_UnboxedPrimitive_Object.arguments(..)) "
                + "&& args(java.lang.Integer)";


        @Pointcut(POINTCUT_EXPRESSION)
        private void arguments() { }

        private static final String BEFORE_ADVICE = Arguments_AspectJ_02UnboxedPrimitiveMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_03UnboxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_UnboxedPrimitive_Object.arguments(..)) "
                + "&& args(int)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_03UnboxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalUnboxedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_UnboxedPrimitive_Object"
                + ".arguments(int)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalUnboxedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testWidenedPrimitive() {
        Argument_WidenedPrimitive_Object object = new Argument_WidenedPrimitive_Object();
        int arg = 1;
        object.arguments(arg);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_WidenedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01WidenedPrimitiveBindning_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02WidenedPrimitiveMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalWidenedPrimitiveBindning_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_12IllegalWidenedPrimitiveMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_WidenedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_WidenedPrimitive_Object.class.getName() + ".arguments";

        public void arguments(int arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01WidenedPrimitiveBindning_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_WidenedPrimitive_Object"
                + ".arguments(int)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01WidenedPrimitiveBindning_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02WidenedPrimitiveMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_WidenedPrimitive_Object.arguments(..)) "
                + "&& args(long)";

        @Pointcut(POINTCUT_EXPRESSION)
        private void arguments() { }

        private static final String BEFORE_ADVICE = Arguments_AspectJ_02WidenedPrimitiveMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalWidenedPrimitiveBindning_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_WidenedPrimitive_Object"
                + ".arguments(long)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalWidenedPrimitiveBindning_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_12IllegalWidenedPrimitiveMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_WidenedPrimitive_Object.arguments(..)) "
                + "&& args(java.lang.Long)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_12IllegalWidenedPrimitiveMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericType() {
        Argument_GenericType_Object<Integer> object = new Argument_GenericType_Object<>();
        object.arguments(1);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01GenericTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02GenericTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalGenericTypeMatching_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_12IllegalGenericTypeMatching_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_GenericType_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_GenericType_Object.class.getName() + ".arguments";


        public void arguments(T arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01GenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericType_Object"
                + ".arguments(java.lang.Number)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01GenericTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02GenericTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericType_Object.arguments(..)) "
                + "&& args(java.lang.Number)";


        @Pointcut(POINTCUT_EXPRESSION)
        public void arguments() {  }

        private static final String BEFORE_ADVICE = Arguments_AspectJ_02GenericTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalGenericTypeMatching_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericType_Object"
                + ".arguments(T))";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalGenericTypeMatching_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_12IllegalGenericTypeMatching_RP_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericType_Object.arguments(..))"
                + "&& args(T)";


        @Pointcut(POINTCUT_EXPRESSION)
        public void arguments() {  }

        private static final String BEFORE_ADVICE = Arguments_AspectJ_12IllegalGenericTypeMatching_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericArray() {
        new Argument_GenericArray_Object<Long>().arguments(new Long[][] {});

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_GenericArray_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01GenericArrayMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02GenericArrayMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalGenericArrayMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_12IllegalGenericArrayMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class Argument_GenericArray_Object<T extends Number & Comparable<T>> {

        public static final String TARGET_METHOD = Argument_GenericArray_Object.class.getName() + ".arguments";

        public void arguments(T[][] arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01GenericArrayMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericArray_Object"
                + ".arguments(java.lang.Number[][]))";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01GenericArrayMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02GenericArrayMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericArray_Object.arguments(..))"
                + "&& args(java.lang.Number[][])";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_02GenericArrayMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalGenericArrayMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericArray_Object"
                + ".arguments(TT[][]))";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalGenericArrayMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_12IllegalGenericArrayMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Argument_GenericArray_Object.arguments(..))"
                + "&& args(TT[][])";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_12IllegalGenericArrayMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testParameterizedCollection() {
        new Arguments_ParameterizedCollection_Object().arguments(new ArrayList<>());

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Arguments_ParameterizedCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01ParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02ParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_03ParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_04ParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_12IllegalParameterizedCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class Arguments_ParameterizedCollection_Object {

        public static final String TARGET_METHOD = Arguments_ParameterizedCollection_Object.class.getName() + ".arguments";

        public void arguments(List<Integer> arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object"
                + ".arguments(java.util.List<java.lang.Integer>)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01ParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object.arguments(..)) "
                + "&& args(java.util.List<java.lang.Integer>)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_02ParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_03ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object"
                + ".arguments(java.util.List)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_03ParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_04ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object.arguments(..)) "
                + "&& args(java.util.List)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_04ParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object"
                + ".arguments(java.util.List<java.lang.String>)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_12IllegalParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_ParameterizedCollection_Object.arguments(..)) "
                + "&& args(java.util.List<java.lang.String>)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_12IllegalParameterizedCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testWildCardCollection() {
        new Arguments_WildCardCollectionMatch_Object().arguments(new ArrayList<>());

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Arguments_WildCardCollectionMatch_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_01WildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_02WildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_03WildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_04WildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_11IllegalWildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_AspectJ_12IllegalWildCardCollectionMatching_Aspect.BEFORE_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class Arguments_WildCardCollectionMatch_Object {

        public static final String TARGET_METHOD = Arguments_WildCardCollectionMatch_Object.class.getName() + ".arguments";

        public void arguments(List<? extends Number> input) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_01WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object"
                + ".arguments(java.util.List<? extends java.lang.Number>)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_01WildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_02WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object.arguments(..)) "
                + "&& args(java.util.List<? extends java.lang.Number>)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_02WildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_03WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object"
                + ".arguments(java.util.List)) ";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_03WildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_04WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object.arguments(..)) "
                + "&& args(java.util.List)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_04WildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_11IllegalWildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object"
                + ".arguments(java.util.List<? super java.lang.Number>))";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_11IllegalWildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Arguments_AspectJ_12IllegalWildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(void io.gemini.aop.integration.Pointcut_03Joinpoint_03Arguments_Tests$Arguments_WildCardCollectionMatch_Object.arguments(..)) "
                + "&& args(java.util.List<? super java.lang.Number>)";


        private static final String BEFORE_ADVICE = Arguments_AspectJ_12IllegalWildCardCollectionMatching_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }
}
