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
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;

/**
 * Tests target object accessing.
 *
 * @author   martin.liu
 */
public class Advice_06AspectJ_02TargetObject_Tests {

    private final static Logger LOGGER = LoggerFactory.getLogger(Advice_06AspectJ_02TargetObject_Tests.class);


    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isNull();;
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_06AspectJ_02TargetObject_Tests$TypeInitializer_Object) && target(targetObject)";


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

        private static final String BEFORE_ADVICE = TypeInitializer_Aspect.class.getName() + ".before";

        @Before(value = TypeInitializer_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, TypeInitializer_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = TypeInitializer_Aspect.class.getName() + ".after";

        @After(value = TypeInitializer_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, TypeInitializer_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TypeInitializer_RP_Aspect {

        @Pointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void targetObject(TypeInitializer_Object targetObject) {  }


        private static final String BEFORE_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, TypeInitializer_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, TypeInitializer_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @Test
    public void testClassMethod() {
        ClassMethod_Object.targetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static void io.gemini.aop.integration.Advice_06AspectJ_02TargetObject_Tests$ClassMethod_Object.targetObject()) && target(targetObject)";


        public static void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(null) );
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = ClassMethod_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before(MutableJoinpoint joinpoint, ClassMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = ClassMethod_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void after(MutableJoinpoint joinpoint, ClassMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class ClassMethod_RP_Aspect {

        @Pointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
        private void targetObject(ClassMethod_Object targetObject) {
            LOGGER.info("targetObject: {}", targetObject);
        }


        private static final String BEFORE_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, ClassMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, ClassMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object expected = new InstanceConstructor_Object(true);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Advice_06AspectJ_02TargetObject_Tests$InstanceConstructor_Object.new(boolean)) && target(targetObject)";


        public InstanceConstructor_Object(boolean targetObject) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before(MutableJoinpoint joinpoint, InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void after(MutableJoinpoint joinpoint, InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class InstanceConstructor_RP_Aspect {

        @Pointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void targetObject(InstanceConstructor_Object targetObject) {  }


        private static final String BEFORE_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object expected = new InstanceMethod_Object();
        expected.targetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Advice_06AspectJ_02TargetObject_Tests$InstanceMethod_Object.targetObject()) && target(targetObject)";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = InstanceMethod_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before(MutableJoinpoint joinpoint, InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = InstanceMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = InstanceMethod_Object.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void after(MutableJoinpoint joinpoint, InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class InstanceMethod_RP_Aspect {

        @Pointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void targetObject(InstanceMethod_Object targetObject) {  }


        private static final String BEFORE_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }


        private static final String AFTER_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @Test
    public void testRenamedArgs() {
        RenamedArgs_Object expected = new RenamedArgs_Object();
        expected.targetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(RenamedArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }
    }

    public static class RenamedArgs_Object {

        private static final String TARGET_METHOD = RenamedArgs_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_02TargetObject_Tests$RenamedArgs_Object.targetObject(..)) && target(targetObject_1)";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @Aspect
    public static class RenamedArgs_Aspect {

        private static final String AFTER_ADVICE = RenamedArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "targetObject_1")
        public void after(MutableJoinpoint joinpoint, RenamedArgs_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class RenamedArgs_RP_Aspect {

        @Pointcut(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "targetObject_1")
        private void targetObject(RenamedArgs_Object targetObject) {
        }

        private static final String AFTER_ADVICE = RenamedArgs_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject_1)", argNames = "targetObject_1")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, RenamedArgs_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withTargetObject(targetObject) );
        }
    }
}
