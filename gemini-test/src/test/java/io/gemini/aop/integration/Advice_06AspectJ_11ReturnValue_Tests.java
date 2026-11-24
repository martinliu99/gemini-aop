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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
@SuppressWarnings("unused")
public class Advice_06AspectJ_11ReturnValue_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_06AspectJ_11ReturnValue_Tests$TypeInitializer_Object)";


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
    public static class TypeInitializer_Aspect {

        private static final String AFTER_ADVICE = TypeInitializer_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = TypeInitializer_Object.POINTCUT_EXPRESSION, returning = "returnValue", argNames = "returnValue")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class TypeInitializer_RP_Aspect {

        @Pointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
        private void returnValue() {  }


        private static final String AFTER_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", returning = "returnValue", argNames = "returnValue")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testClassMethod() {
        int expected = ClassMethod_Object.returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.Advice_06AspectJ_11ReturnValue_Tests$ClassMethod_Object.returnValue())";


        public static int returnValue() {
            int returnValue = 100;
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = ClassMethod_Object.POINTCUT_EXPRESSION, returning = "returnValue", argNames = "returnValue")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ClassMethod_RP_Aspect {

        @Pointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
        private void returnValue() {  }


        private static final String AFTER_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", returning = "returnValue", argNames = "returnValue")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        new InstanceConstructor_Object(1);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Advice_06AspectJ_11ReturnValue_Tests$InstanceConstructor_Object.new(int))";


        public InstanceConstructor_Object(int returnValue) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, returning = "returnValue", argNames = "returnValue")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class InstanceConstructor_RP_Aspect {

        @Pointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        private void returnValue() {  }


        private static final String AFTER_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", returning = "returnValue", argNames = "returnValue")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object object = new InstanceMethod_Object();

        int expected = object.returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_11ReturnValue_Tests$InstanceMethod_Object.returnValue())";


        public int returnValue() {
            int returnValue = 100;
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = InstanceMethod_Object.POINTCUT_EXPRESSION, returning = "returnValue", argNames = "returnValue")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class InstanceMethod_RP_Aspect {

        @Pointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
        private void returnValue() {  }


        private static final String AFTER_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", returning = "returnValue", argNames = "returnValue")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testRenamedArgs() {
        RenamedArgs_Object object = new RenamedArgs_Object();
        Object expected = object.returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(RenamedArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }
    }

    public static class RenamedArgs_Object {

        private static final String TARGET_METHOD = RenamedArgs_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_11ReturnValue_Tests$RenamedArgs_Object.returnValue(..))";


        public int returnValue() {
            int returnValue = 100;
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }

    @Aspect
    public static class RenamedArgs_Aspect {

        private static final String AFTER_ADVICE = RenamedArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "returnValue_1", returning = "returnValue_1")
        public void after(MutableJoinpoint joinpoint, int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class RenamedArgs_RP_Aspect {

        @Pointcut(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "returnValue_1")
        private void returnValue(int returnValue) {
        }

        private static final String AFTER_ADVICE = RenamedArgs_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue(returnValue_1)", argNames = "returnValue_1", returning = "returnValue_1")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withReturnValue(returnValue) );
        }
    }
}
