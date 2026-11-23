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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
 * Tests joinpoint descriptor accessing.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_01Descriptor_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(TypeInitializer_Object.class);
            assertThat(beforeAdviceMethodInvoker.getStaticPart()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            assertThat(afterAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(afterAdviceMethodInvoker.getTargetClass()).isEqualTo(TypeInitializer_Object.class);
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".descriptor";

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

        private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_01Descriptor_Tests$TypeInitializer_Object)";


        private static final String BEFORE_ADVICE = TypeInitializer_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass())
                        .withStaticPart(joinpoint.getStaticPart()) );
        }


        private static final String AFTER_ADVICE = TypeInitializer_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass())
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testClassMethod() {
        ClassMethod_Object.descriptor();

        Class<?> exptectedTargetClass = null;
        AccessibleObject expectedTargetMethod = null;
        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            exptectedTargetClass = targetMethodInvoker.getTargetClass();
            expectedTargetMethod = targetMethodInvoker.getStaticPart();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            assertThat(afterAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(afterAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }
    }

    public static class ClassMethod_Object {

        public static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".descriptor";

        private static final String TARGET_METHOD_NAME = "descriptor";
 
        public static void descriptor() {
            Method method = null;
            try {
                method = ClassMethod_Object.class.getMethod(ClassMethod_Object.TARGET_METHOD_NAME);
            } catch (Exception e) { /* do nothing */ }

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetClass(ClassMethod_Object.class)
                        .withStaticPart(method) );
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static void io.gemini.aop.integration.MutableJoinpoint_01Descriptor_Tests$ClassMethod_Object.descriptor())";


        private static final String BEFORE_ADVICE = ClassMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }


        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        new InstanceConstructor_Object( (byte)1);

        Class<?> exptectedTargetClass = null;
        AccessibleObject expectedTargetMethod = null;
        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            exptectedTargetClass = targetMethodInvoker.getTargetClass();
            expectedTargetMethod = targetMethodInvoker.getStaticPart();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            assertThat(afterAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(afterAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }
    }

    public static class InstanceConstructor_Object {

        public static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".descriptor";


        public InstanceConstructor_Object(byte descriptor) {
            Constructor<?> method = null;
            try {
                method = InstanceConstructor_Object.class.getConstructor(byte.class);
            } catch (Exception e) { /* do nothing */ }

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetClass(InstanceConstructor_Object.class)
                        .withStaticPart(method) );
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_01Descriptor_Tests$InstanceConstructor_Object.new(byte))";


        private static final String BEFORE_ADVICE = InstanceConstructor_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }


        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object expected = new InstanceMethod_Object();
        expected.descriptor();

        Class<?> exptectedTargetClass = null;
        AccessibleObject expectedTargetMethod = null;
        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            exptectedTargetClass = targetMethodInvoker.getTargetClass();
            expectedTargetMethod = targetMethodInvoker.getStaticPart();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            assertThat(afterAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(afterAdviceMethodInvoker.getTargetClass()).isEqualTo(exptectedTargetClass);
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(expectedTargetMethod);
        }
    }

    public static class InstanceMethod_Object {

        public static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".descriptor";


        public void descriptor() {
            Method method = null;
            try {
                method = InstanceMethod_Object.class.getMethod("descriptor");
            } catch (Exception e) { /* do nothing */ }

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetClass(InstanceMethod_Object.class)
                        .withStaticPart(method) );
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.MutableJoinpoint_01Descriptor_Tests$InstanceMethod_Object.descriptor())";


        private static final String BEFORE_ADVICE = InstanceMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }


        private static final String AFTER_ADVICE = InstanceMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }
}
