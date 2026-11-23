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

import java.io.IOException;

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
 * Tests thrown exception accessing of target method.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_12BypassingThrownException_Tests {

    private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targeThrownException";

    private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_12BypassingThrownException_Tests$TypeInitializer_Object)";


    @Test
    public void testTypeInitializer() {
        Throwable thrownException = null;
        try {
            new TypeInitializer_Object();

            assertThat(false).isTrue();
        } catch (ExceptionInInitializerError e) {
            thrownException = e.getCause();
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(TypeInitializer_02Assiging_Aspect.BEFORE_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class TypeInitializer_Object {

        static {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            int i = 0;
            if (i ==0)
                throw thrownException;
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
            Object returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class TypeInitializer_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            RuntimeException thrownException = new ClassCastException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
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
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }


    @Test
    public void testClassMethod() {
        Throwable thrownException = null;
        try {
            ClassMethod_Object.thrownException();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(ClassMethod_02Assiging_Aspect.BEFORE_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_3Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.MutableJoinpoint_12BypassingThrownException_Tests$ClassMethod_Object.thrownException())";


        public static void thrownException() throws IOException {
            RuntimeException thrownException  = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class ClassMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ClassMethod_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ClassMethod_3Accessing_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_3Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        Throwable thrownException = null;
        try {
            new InstanceConstructor_Object();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(InstanceConstructor_02Assiging_Aspect.BEFORE_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targeThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_12BypassingThrownException_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() throws IOException {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class InstanceConstructor_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03Accessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_03Accessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        Throwable thrownException = null;
        try {
            new InstanceMethod_Object().thrownException();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_02Assiging_Aspect.BEFORE_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Accessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.MutableJoinpoint_12BypassingThrownException_Tests$InstanceMethod_Object.thrownException())";


        public void thrownException() throws IOException {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class InstanceMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Object returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceMethod_02Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_02Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
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
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }    
}
