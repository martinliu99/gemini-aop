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
 * Tests thrown exception accessing of target method.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_12ThrownException_Tests {

    private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targeThrownException";

    private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_12ThrownException_Tests$TypeInitializer_Object)";


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
                .getAdviceMethodInvoker(TypeInitializer_03Assiging_Aspect.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
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
    public static class TypeInitializer_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getThrownException();
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
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class TypeInitializer_03Assiging_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            RuntimeException thrownException = new ClassCastException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class TypeInitializer_04Assiging_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 4;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class TypeInitializer_05Accessing_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_05Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
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
                .getAdviceMethodInvoker(ClassMethod_03Assiging_Aspect.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.MutableJoinpoint_12ThrownException_Tests$ClassMethod_Object.thrownException())";


        public static void thrownException() throws IOException {
            RuntimeException thrownException  = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class ClassMethod_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getThrownException();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
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
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ClassMethod_03Assiging_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class ClassMethod_04Assiging_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 4;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
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

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isInstanceOf(RuntimeException.class);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_05IllegalAccessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_04IllegalAssiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03IllegalAssiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02IllegalAccessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targeThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_12ThrownException_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class InstanceConstructor_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getThrownException();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
                            .withThrownException(e) );
            }
        }
    }

    @Aspect
    public static class InstanceConstructor_02IllegalAccessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_02IllegalAccessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03IllegalAssiging_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_03IllegalAssiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_04IllegalAssiging_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_04IllegalAssiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 4;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_05IllegalAccessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_05IllegalAccessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
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
                .getAdviceMethodInvoker(InstanceMethod_03Assiging_Aspect.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_05Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01IllegalAccessing_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getThrownException()).isInstanceOf(IllegalStateException.class);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.MutableJoinpoint_12ThrownException_Tests$InstanceMethod_Object.thrownException())";


        public void thrownException() throws IOException {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @Aspect
    public static class InstanceMethod_01IllegalAccessing_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01IllegalAccessing_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            try {
                joinpoint.getThrownException();
            } catch (Exception e) {
                ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                        new AdviceMethod()
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
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceMethod_03Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_03Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Exception thrownException = new IOException();
            joinpoint.setThrownException(thrownException);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }

    @Aspect
    public static class InstanceMethod_04Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_04Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            Object returnValue = 4;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class InstanceMethod_05Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_05Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }    
}
