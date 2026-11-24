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

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;

/**
 * Tests thrown exception accessing of target method.
 *
 * @author   martin.liu
 */
@SuppressWarnings("unused")
public class Advice_06AspectJ_12ThrownException_Tests {

    private static final String TARGET_METHOD = TypeInitializer_Aspect.class.getName() + ".thrownException";

    private static final String POINTCUT_EXPRESSION = 
            "staticinitialization(io.gemini.aop.integration.Advice_06AspectJ_12ThrownException_Tests$TypeInitializer_Object)";


    @Test
    public void testTypeInitializer() {
        Throwable thrownException = null;
        try {
            new TypeInitializer_Object();
        } catch (ExceptionInInitializerError e) {
            thrownException = e.getCause();
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        try {
            new TypeInitializer_Object();
        } catch (Throwable t) {
            assertThat(t).isInstanceOf(NoClassDefFoundError.class);
        }
    }

    public static class TypeInitializer_Object {

        static {
            try {
                throwException();
            } catch (Throwable e) {
                ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                        new TargetMethod()
                            .withThrownException(e) );
                throw e;
            }
        }

        private static void throwException() {
            throw new RuntimeException();
        }
    }

    @Aspect
    public static class TypeInitializer_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = POINTCUT_EXPRESSION, throwing = "thrownException", argNames = "thrownException")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class TypeInitializer_RP_Aspect {

        @Pointcut(POINTCUT_EXPRESSION)
        public void thrownException() {  }

        private static final String AFTER_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException()", throwing = "thrownException", argNames = "thrownException")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testClassMethod() {
        Throwable thrownException = null;
        try {
            ClassMethod_Object.thrownException();
            assertThat(false).isTrue();
        } catch (IOException e) {
            thrownException = e;
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.Advice_06AspectJ_12ThrownException_Tests$ClassMethod_Object.thrownException())";


        public static void thrownException() throws IOException {
            try {
            IOException exp = new IOException();

            throw exp;
            } catch (IOException e) {
                ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                        new TargetMethod()
                            .withThrownException(e) );
                throw e;
            }
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = ClassMethod_Object.POINTCUT_EXPRESSION, throwing = "thrownException", argNames = "thrownException")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class ClassMethod_RP_Aspect {

        @Pointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void thrownException() {  }


        private static final String AFTER_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException()", throwing = "thrownException", argNames = "thrownException")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        Throwable thrownException = null;
        try {
            new InstanceConstructor_Object(1l);
            assertThat(false).isTrue();
        } catch (IOException e) {
            thrownException = e;
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Advice_06AspectJ_12ThrownException_Tests$InstanceConstructor_Object.new(long))";


        public InstanceConstructor_Object(long thrownException) throws IOException {
            IOException exp = new IOException();
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withThrownException(exp) );

            throw exp;
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, throwing = "thrownException", argNames = "thrownException")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class InstanceConstructor_RP_Aspect {

        @Pointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void thrownException() {  }


        private static final String AFTER_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException()", throwing = "thrownException", argNames = "thrownException")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object targetObject = new InstanceMethod_Object();

        Throwable thrownException = null;
        try {
            targetObject.thrownException();
            assertThat(false).isTrue();
        } catch (IOException expected) {
            thrownException = expected;
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_12ThrownException_Tests$InstanceMethod_Object.thrownException())";


        public void thrownException() throws IOException {
            try {
                IOException exp = new IOException();
            throw exp;
            } catch (IOException e) {
                ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                        new TargetMethod()
                            .withThrownException(e) );

                throw e;
            }
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = InstanceMethod_Object.POINTCUT_EXPRESSION, throwing = "thrownException", argNames = "thrownException")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class InstanceMethod_RP_Aspect {

        @Pointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void thrownException() {  }


        private static final String AFTER_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException()", throwing = "thrownException", argNames = "thrownException")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testRenamedArgs() {
        RenamedArgs_Object targetObject = new RenamedArgs_Object();

        Throwable thrownException = null;
        try {
            targetObject.thrownException();
            assertThat(false).isTrue();
        } catch (IOException expected) {
            thrownException = expected;
        }

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(RenamedArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }
    }

    public static class RenamedArgs_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".thrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_12ThrownException_Tests$RenamedArgs_Object.thrownException())";


        public void thrownException() throws IOException {
            try {
                IOException exp = new IOException();
            throw exp;
            } catch (IOException e) {
                ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                        new TargetMethod()
                            .withThrownException(e) );

                throw e;
            }
        }
    }

    @Aspect
    public static class RenamedArgs_Aspect {

        private static final String AFTER_ADVICE = RenamedArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "thrownException_1", throwing = "thrownException_1")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @Aspect
    public static class RenamedArgs_RP_Aspect {

        @Pointcut(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "thrownException_1")
        private void thrownException(Throwable thrownException) {
        }

        private static final String AFTER_ADVICE = RenamedArgs_RP_Aspect.class.getName() + ".after";

        @AfterThrowing(value = "thrownException(thrownException_1)", argNames = "thrownException_1", throwing = "thrownException_1")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withThrownException(thrownException) );
        }
    }
}
