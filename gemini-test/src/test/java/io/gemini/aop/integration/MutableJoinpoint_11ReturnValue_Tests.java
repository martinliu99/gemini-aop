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

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;

/**
 * Tests return value accessing of target method.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_11ReturnValue_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();
        Object exptected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_11ReturnValue_Tests$TypeInitializer_Object)";


        static {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }

        static final Logger LOGGER;

        static {
            LOGGER = LoggerFactory.getLogger(TypeInitializer_Object.class);
        }
    }

    @Aspect
    public static class TypeInitializer_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getReturnValue();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withThrownException(e) );
            }
        }
    }

    @Aspect
    public static class TypeInitializer_02Accessing_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_02Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class TypeInitializer_03Assiging_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 3;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class TypeInitializer_04Assiging_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class TypeInitializer_05Accessing_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_05Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testClassMethod() {
        int returnValue = ClassMethod_Object.returnValue();
        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(ClassMethod_03Assiging_Aspect.AFTER_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(
                    (int) ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD).getReturnValue() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(null);
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.MutableJoinpoint_11ReturnValue_Tests$ClassMethod_Object.returnValue())";


        public static int returnValue() {
            int returnValue = 100;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @Aspect
    public static class ClassMethod_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getReturnValue();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withReturnValue(null)
                            .withThrownException(e) );
            }
        }
    }

    @Aspect
    public static class ClassMethod_02Accessing_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_02Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ClassMethod_03Assiging_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 3;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ClassMethod_04Assiging_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ClassMethod_05Accessing_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_05Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object expected = new InstanceConstructor_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();;
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(null);
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_11ReturnValue_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }

        public InstanceConstructor_Object(boolean flag) {
        }
    }

    @Aspect
    public static class InstanceConstructor_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getReturnValue();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withReturnValue(null)
                            .withThrownException(e) );
            }
        }
    }

    @Aspect
    public static class InstanceConstructor_02Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_02Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            InstanceConstructor_Object returnValue = new InstanceConstructor_Object(true);
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_04Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_05Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_05Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        int returnValue = new InstanceMethod_Object().returnValue();
        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_03Assiging_Aspect.AFTER_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_05AccessingAspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD).getReturnValue() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(null);
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.MutableJoinpoint_11ReturnValue_Tests$InstanceMethod_Object.returnValue())";


        public int returnValue() {
            int returnValue = 100;
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }

    @Aspect
    public static class InstanceMethod_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getReturnValue();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withReturnValue(null)
                            .withThrownException(e) );
            }
        }
    }

    @Aspect
    public static class InstanceMethod_02Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_02Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceMethod_03Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            int returnValue = 3;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceMethod_04Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceMethod_05AccessingAspect {

        private static final String AFTER_ADVICE = InstanceMethod_05AccessingAspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }
}
