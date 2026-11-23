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
 * Tests target object accessing.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_02TargetObject_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();
        Object expected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_02TargetObject_Tests$TypeInitializer_Object)";


        static final Logger LOGGER;

        static {
            LOGGER = LoggerFactory.getLogger(TypeInitializer_Object.class);
        }

        static {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @Aspect
    public static class TypeInitializer_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            TypeInitializer_Object targetObject = new TypeInitializer_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
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
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }


    @Test
    public void testClassMethod() {
        ClassMethod_Object.targetObject();
        Object expected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isNull();
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static void io.gemini.aop.integration.MutableJoinpoint_02TargetObject_Tests$ClassMethod_Object.targetObject())";


        public static void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(null) );
        }
    }

    @Aspect
    public static class ClassMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ClassMethod_Object targetObject = new ClassMethod_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
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
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object object = new InstanceConstructor_Object(true);
        Object expected = (InstanceConstructor_Object) ExecutionMemento
                .getAdviceMethodInvoker(InstanceConstructor_02Assiging_Aspect.AFTER_ADVICE)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            Object captured = targetMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(object);
            assertThat(captured).isNotEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            Object captured = beforeAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isNotNull();
            assertThat(captured).isNotEqualTo(object);
            assertThat(captured).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Assiging_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            Object captured = afterAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(object);
            assertThat(captured).isNotEqualTo(expected);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_02TargetObject_Tests$InstanceConstructor_Object.new(boolean))";


        public InstanceConstructor_Object(boolean targetObject) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }

        public InstanceConstructor_Object() {}
    }

    @Aspect
    public static class InstanceConstructor_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            InstanceConstructor_Object targetObject = new InstanceConstructor_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_02Assiging_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_02Assiging_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            InstanceConstructor_Object targetObject = new InstanceConstructor_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_03Accessing_Aspect.class.getName() + ".after2";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after2(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object object = new InstanceMethod_Object();
        object.targetObject();

        InstanceMethod_Object expected = (InstanceMethod_Object) ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_01Assiging_Aspect.BEFORE_ADVICE)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            Object captured = targetMethodInvoker.getTargetObject();
            assertThat(captured).isNotEqualTo(object);
            assertThat(captured).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            Object captured = beforeAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isNotEqualTo(object);
            assertThat(captured).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            Object captured = afterAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(expected);
            assertThat(captured).isNotEqualTo(object);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.MutableJoinpoint_02TargetObject_Tests$InstanceMethod_Object.targetObject())";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @Aspect
    public static class InstanceMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            InstanceMethod_Object targetObject = new InstanceMethod_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
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
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }
}
