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
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.zip.Deflater;

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
import io.gemini.api.aop.annotation.ConditionalOnClassLoader;

/**
 * Tests method matching, e.g., type initializer, class method, constructor, instance method or native method, etc.
 *
 * @author   martin.liu
 */
public class Target_02TargetMethod_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod adviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.BEFORE_ADVICE);
            assertThat(adviceMethodInvoker).isNotNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".<clinit>";

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

        private static final String BEFORE_ADVICE = TypeInitializer_Aspect.class.getName() + ".typeInitializer_before";

        @SuppressWarnings("rawtypes")
        @Before("staticinitialization(io.gemini.aop.integration.Target_02TargetMethod_Tests$TypeInitializer_Object)")
        public void typeInitializer_before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testClassMethod() {
        ClassMethod_Object.classMethod();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    public static class ClassMethod_Object {

        public static void classMethod() {
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public static void io.gemini.aop.integration.Target_02TargetMethod_Tests$ClassMethod_Object.classMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testInstanceConstructor() {
        new InstanceConstructor_Object( (byte)1);

        AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
        assertThat(afterAdviceMethodInvoker).isNotNull();
    }

    public static class InstanceConstructor_Object {

        public InstanceConstructor_Object(byte input) {
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @After("execution(public io.gemini.aop.integration.Target_02TargetMethod_Tests$InstanceConstructor_Object.new(byte))")
        public void after(MutableJoinpoint<Void, RuntimeException> joinpoint) {
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
        expected.instanceMethod();

        AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
        assertThat(afterAdviceMethodInvoker).isNotNull();
    }

    public static class InstanceMethod_Object {

        public void instanceMethod() {
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public void io.gemini.aop.integration.Target_02TargetMethod_Tests$InstanceMethod_Object.instanceMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testInterfaceMethod() {
        InterfaceMethod_Object expected = new InterfaceMethod_Object.Implementor();
        expected.interfaceMethod();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InterfaceMethod_Interface_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(InterfaceMethod_Object.Implementor.IMPLEMENTOR_INTERFACE_METHOD);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InterfaceMethod_Implementor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(InterfaceMethod_Object.Implementor.IMPLEMENTOR_INTERFACE_METHOD);
        }
    }

    static interface InterfaceMethod_Object {

        void interfaceMethod();


        static class Implementor implements InterfaceMethod_Object {

            public static final Method IMPLEMENTOR_INTERFACE_METHOD;

            static {
                Method implementorInterfaceMethod = null;
                try {
                    implementorInterfaceMethod = Implementor.class.getDeclaredMethod("interfaceMethod");
                } catch (Exception e) { /* do nothing */ }

                IMPLEMENTOR_INTERFACE_METHOD = implementorInterfaceMethod;
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            public void interfaceMethod() {
            }
        }
    }

    @Aspect
    public static class InterfaceMethod_Interface_Aspect {

        private static final String AFTER_ADVICE = InterfaceMethod_Interface_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("within(io.gemini.aop.integration.Target_02TargetMethod_Tests) && execution(public void io.gemini.aop.integration.Target_02TargetMethod_Tests$InterfaceMethod_Object+.interfaceMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

    @Aspect
    public static class InterfaceMethod_Implementor_Aspect {

        private static final String AFTER_ADVICE = InterfaceMethod_Interface_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public void io.gemini.aop.integration.Target_02TargetMethod_Tests$InterfaceMethod_Object$Implementor.interfaceMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }

    }


    @Test
    public void testDefaultMethod() {
        new DefaultMethod_Object.Implementor().defaultMethod();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DefaultMethod_Interface_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(DefaultMethod_Object.Implementor.INTERFACE_DEFAULT_METHOD);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DefaultMethod_Implementor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(DefaultMethod_Object.Implementor.IMPLEMENTOR_DEFAULT_METHOD);
        }
    }

    static interface DefaultMethod_Object {

        default void defaultMethod() {
        }


        static class Implementor implements DefaultMethod_Object {

            public static final Method INTERFACE_DEFAULT_METHOD;
            public static final Method IMPLEMENTOR_DEFAULT_METHOD;

            static {
                Method interfaceDefaultMethod = null;
                Method implementorDefaultMethod = null;
                try {
                    interfaceDefaultMethod = DefaultMethod_Object.class.getDeclaredMethod("defaultMethod");
                    implementorDefaultMethod = DefaultMethod_Object.Implementor.class.getDeclaredMethod("defaultMethod");
                } catch (Exception e) { /* do nothing */ }

                INTERFACE_DEFAULT_METHOD = interfaceDefaultMethod;
                IMPLEMENTOR_DEFAULT_METHOD = implementorDefaultMethod;
            }

            public void defaultMethod() {
                DefaultMethod_Object.super.defaultMethod();
            }
        }
    }
 
    @Aspect
    public static class DefaultMethod_Interface_Aspect {

        private static final String AFTER_ADVICE = InterfaceMethod_Interface_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public void io.gemini.aop.integration.Target_02TargetMethod_Tests$DefaultMethod_Object.defaultMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

    @Aspect
    public static class DefaultMethod_Implementor_Aspect {

        private static final String AFTER_ADVICE = DefaultMethod_Implementor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public void io.gemini.aop.integration.Target_02TargetMethod_Tests$DefaultMethod_Object$Implementor.defaultMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testGenericBridgeMethod() {
        new GenericBridgeMethod_Object.Implementor().genericBridgeMethod();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(GenericBridgeMethod_InheritedMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(GenericBridgeMethod_Object.Implementor.TARGET_METHOD);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(GenericBridgeMethod_BridgeMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(GenericBridgeMethod_Object.Implementor.TARGET_METHOD);
        }
    }

    static interface GenericBridgeMethod_Object<T> {

        T genericBridgeMethod();


        public static class Implementor implements GenericBridgeMethod_Object<String> {

            private static final Method TARGET_METHOD;

            static {
                Method genericBridge = null;
                try {
                    genericBridge = Implementor.class.getDeclaredMethod("genericBridgeMethod");
                } catch (Exception e) { /* do nothing */ }

                TARGET_METHOD = genericBridge;
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            public String genericBridgeMethod() {
                return null;
            }
        }
    }

    @Aspect
    public static class GenericBridgeMethod_InheritedMethod_Aspect {

        private static final String AFTER_ADVICE = GenericBridgeMethod_InheritedMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public java.lang.Object io.gemini.aop.integration.Target_02TargetMethod_Tests$GenericBridgeMethod_Object$Implementor.genericBridgeMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

    @Aspect
    public static class GenericBridgeMethod_BridgeMethod_Aspect {

        private static final String AFTER_ADVICE = GenericBridgeMethod_BridgeMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public java.lang.String io.gemini.aop.integration.Target_02TargetMethod_Tests$GenericBridgeMethod_Object$Implementor.genericBridgeMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testCovariantBridgeMethod() {
        new CovariantBridgeMethod_Object.Implementor().covariantBridgeMethod();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(CovariantBridgeMethod_InheritedMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(CovariantBridgeMethod_Object.Implementor.TARGET_METHOD);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(CovariantBridgeMethod_BridgeMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getStaticPart()).isEqualTo(CovariantBridgeMethod_Object.Implementor.TARGET_METHOD);
        }
    }

    static interface CovariantBridgeMethod_Object {

        public Number covariantBridgeMethod();


        public static class Implementor implements CovariantBridgeMethod_Object {

            private static final Method TARGET_METHOD;

            static {
                Method covariantBridge = null;

                try {
                    covariantBridge = Implementor.class.getDeclaredMethod("covariantBridgeMethod");
                } catch (Exception e) { /* do nothing */ }

                TARGET_METHOD = covariantBridge;
            }

            /** 
             * {@inheritDoc} 
             */
            @Override
            public Long covariantBridgeMethod() {
                return null;
            }
        }
    }

    @Aspect
    public static class CovariantBridgeMethod_InheritedMethod_Aspect {

        private static final String AFTER_ADVICE = CovariantBridgeMethod_InheritedMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public java.lang.Number io.gemini.aop.integration.Target_02TargetMethod_Tests$CovariantBridgeMethod_Object$Implementor.covariantBridgeMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

    @Aspect
    public static class CovariantBridgeMethod_BridgeMethod_Aspect {

        private static final String AFTER_ADVICE = CovariantBridgeMethod_BridgeMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After("execution(public java.lang.Long io.gemini.aop.integration.Target_02TargetMethod_Tests$CovariantBridgeMethod_Object$Implementor.covariantBridgeMethod())")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testNativeMethod() {
        {
            byte[] data = "This is a long text that needs compression".getBytes(Charset.defaultCharset());

            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();

            deflater.end();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(NativeMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

            assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(Deflater.class);
            assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(NativeMethod_Aspect.TARGET_METHOD);
        }
    }

    @Aspect
    public static class NativeMethod_Aspect {

        private static final String BEFORE_ADVICE = NativeMethod_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = Deflater.class.getDeclaredMethod("init", int.class, int.class, boolean.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @ConditionalOnClassLoader(isBootstrapClassLoader = true)
        @Before("execution(private native static long java.util.zip.Deflater.init(int, int, boolean))")
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }
}
