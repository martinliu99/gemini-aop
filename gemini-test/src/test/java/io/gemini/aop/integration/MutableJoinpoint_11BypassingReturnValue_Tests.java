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
public class MutableJoinpoint_11BypassingReturnValue_Tests {

    private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".returnValue";

    private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_11BypassingReturnValue_Tests$TypeInitializer_Object)";


    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();
        Object exptected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Assiging_Aspect.BEFORE_ADVICE);            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }
    }

    public static class TypeInitializer_Object {


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
    public static class TypeInitializer_01Assiging_Aspect {


        private static final String BEFORE_ADVICE = TypeInitializer_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class TypeInitializer_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 2;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class TypeInitializer_03Accessing_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_03Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testClassMethod() {
        int returnValue = 0;
        try {
            returnValue = ClassMethod_Object.returnValue();
        } catch (Exception e) {
            assertThat(false).isTrue();
        }

        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(ClassMethod_02Assiging_Aspect.BEFORE_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(null);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(returnValue);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.MutableJoinpoint_11BypassingReturnValue_Tests$ClassMethod_Object.returnValue())";


        public static int returnValue() throws Exception {
            int returnValue = 100;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @Aspect
    public static class ClassMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ClassMethod_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 2;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ClassMethod_03Accessing_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_03Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object expected = null;
        try {
            expected = new InstanceConstructor_Object();
        } catch (Exception e) {
            assertThat(false).isTrue();
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_11BypassingReturnValue_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() throws Exception {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }

        public InstanceConstructor_Object(boolean flag) {
        }
    }

    @Aspect
    public static class InstanceConstructor_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(null)
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class InstanceConstructor_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            InstanceConstructor_Object returnValue = new InstanceConstructor_Object(true);
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03Accessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_03Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes" })
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        int returnValue = 0;
        try {
            returnValue = new InstanceMethod_Object().returnValue();
        } catch (Exception e) {
            assertThat(e).isNull();
        }

        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_03Accessing_Aspect.BEFORE_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(null);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.MutableJoinpoint_11BypassingReturnValue_Tests$InstanceMethod_Object.returnValue())";


        public int returnValue() throws Exception {
            int returnValue = 100;
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }

    @Aspect
    public static class InstanceMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new NoSuchMethodException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceMethod_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 2;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceMethod_03Accessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_03Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }
}
