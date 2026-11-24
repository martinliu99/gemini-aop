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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletException;

import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.MDC;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.activation.registries.MimeTypeFile;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ConditionalOnClassLoader;
import io.gemini.api.aop.annotation.EnableCircularityBreaker;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Tests target classes, e.g., class loaded by bootstrap class loader, class version lower than java 8, etc.
 *
 * @author   martin.liu
 */
public class Target_01TargetClass_Tests {

    private static final Logger LOGGER = LoggerFactory.getLogger(Target_01TargetClass_Tests.class);


    @Test
    public void testJdkClass1() {
        String object = new String("1,2,3,");
        String[] result = object.split(",");

        assertThat(result).isNotNull();

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(JdkClass1_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(String.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(JdkClass1_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class JdkClass1_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.String[] java.lang.String.split(java.lang.String))";

        private static final String BEFORE_ADVICE = JdkClass1_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = String.class.getDeclaredMethod("split", String.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @ConditionalOnClassLoader(isBootstrapClassLoader = true)
        @EnableCircularityBreaker
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

    @ConditionalOnClassLoader(isBootstrapClassLoader = true)
//    @ExprPointcut(JdkClass1_Aspect.POINTCUT_EXPRESSION)
    @EnableCircularityBreaker
    public static class TestAdvice1 {

        @Advice.OnMethodEnter(inline = false, prependLineNumber = true)
        public static void beforeInstanceMethod(
                @Advice.This Object targetObject,
                @Advice.AllArguments(readOnly = true, typing = Assigner.Typing.DYNAMIC) Object[] arguments
        ) throws Throwable {
//            System.out.println("before InstanceMethod: ");
        }


        @Advice.OnMethodExit(inline = false)
        public static void afterInstanceMethod(
                ) throws Throwable {
//            System.out.println("after InstanceMethod");
        }
    }

    @Test
    public void testJdkClass2() {
        Executor executor = Executors.newCachedThreadPool();
        executor.execute(() -> LOGGER.info("Test JDK ThreadPoolExecutor."));

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(JdkClass2_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(ThreadPoolExecutor.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(JdkClass2_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class JdkClass2_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void java.util.concurrent.ThreadPoolExecutor.execute(java.lang.Runnable))";

        private static final String BEFORE_ADVICE = JdkClass2_Aspect.class.getName() + ".jdkClass_before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = ThreadPoolExecutor.class.getDeclaredMethod("execute", Runnable.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @ConditionalOnClassLoader(isBootstrapClassLoader = true)
        @Before(POINTCUT_EXPRESSION)
        public void jdkClass_before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }

//    @Advisor(inheritClassLoaderMatcher = false, inheritTypeMatcher = false, perInstance = false)
//    @ExprPointcut(pointcutExpression = JdkClass2_Aspect.POINTCUT_EXPRESSION)
    public static class TestAdvice {

        private static final String BEFORE_ADVICE = TestAdvice.class.getName() + ".before";

        @Advice.OnMethodEnter(inline = false, prependLineNumber = true)
        public static void before(
                @Advice.This Object targetObject
//                @Advice.AllArguments(readOnly = true, typing = Assigner.Typing.DYNAMIC) Object[] arguments
        ) throws Throwable {
            System.out.println("before InstanceMethod: " + targetObject);
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }


        @Advice.OnMethodExit(inline = false)
        public static void after(
                ) throws Throwable {
            System.out.println("after InstanceMethod");
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testByteCode1x() {
        ToStringBuilder object = new ToStringBuilder(new Object());
        object.append("name", "test");

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode1x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(ToStringBuilder.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode1x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode1x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private org.apache.commons.lang.builder.ToStringBuilder org.apache.commons.lang.builder.ToStringBuilder.append(..))";

        private static final String BEFORE_ADVICE = ByteCode1x_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = ToStringBuilder.class.getMethod("append", String.class, Object.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testByteCode2x() {
        BooleanConverter converter = new BooleanConverter();
        converter.convert(Boolean.class, "true");

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode2x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(BooleanConverter.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode2x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode2x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Object org.apache.commons.beanutils.converters.BooleanConverter.convert(..))";

        private static final String BEFORE_ADVICE = ByteCode2x_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = BooleanConverter.class.getMethod("convert", Class.class, Object.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testByteCode3x() {
        Option option = new Option("", "");
        option.getArgs();

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode3x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(Option.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode3x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode3x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public int org.apache.commons.cli.Option.getArgs())";

        private static final String BEFORE_ADVICE = ByteCode2x_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = Option.class.getMethod("getArgs");
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testByteCode4x() {
        new MimeTypeFile();

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode4x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(MimeTypeFile.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode4x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode4x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public com.sun.activation.registries.MimeTypeFile.new())";

        private static final String BEFORE_ADVICE = ByteCode2x_Aspect.class.getName() + ".before";

        private static final Constructor<MimeTypeFile> TARGET_METHOD;

        static {
            Constructor<MimeTypeFile> constructor = null;
            try {
                constructor = MimeTypeFile.class.getConstructor();
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = constructor;
        }


        @SuppressWarnings("rawtypes")
        @ConditionalOnClassLoader(classLoaderExpression = "AppClassLoader")
        @After(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );

            LOGGER.info("Target object: {}", (MimeTypeFile) joinpoint.getTargetObject());
        }
    }


    @Test
    public void testByteCode5x() {
        ServletException exception = new ServletException();
        exception.getRootCause();

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode5x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(ServletException.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode5x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode5x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public java.lang.Throwable javax.servlet.ServletException.getRootCause(..))";

        private static final String BEFORE_ADVICE = ByteCode2x_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = ServletException.class.getMethod("getRootCause");
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }


    @Test
    public void testByteCode6x() {
        MDC.get("");

        AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ByteCode6x_Aspect.BEFORE_ADVICE);
        assertThat(beforeAdviceMethodInvoker).isNotNull();

        assertThat(beforeAdviceMethodInvoker.getTargetLookup()).isNotNull();
        assertThat(beforeAdviceMethodInvoker.getTargetLookup().lookupModes() & Lookup.PRIVATE).isNotEqualTo(0);

        assertThat(beforeAdviceMethodInvoker.getTargetClass()).isEqualTo(MDC.class);
        assertThat(beforeAdviceMethodInvoker.getStaticPart()).isEqualTo(ByteCode6x_Aspect.TARGET_METHOD);
    }

    @Aspect
    public static class ByteCode6x_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static java.lang.Object org.apache.log4j.MDC.get(java.lang.String))";

        private static final String BEFORE_ADVICE = ByteCode2x_Aspect.class.getName() + ".before";

        private static final Method TARGET_METHOD;

        static {
            Method method = null;
            try {
                method = MDC.class.getMethod("get", String.class);
            } catch (Exception e) { /* do nothing */ }
            TARGET_METHOD = method;
        }


        @SuppressWarnings("rawtypes")
        @Before(POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetLookup(joinpoint.getTargetLookup())
                        .withTargetClass(joinpoint.getTargetClass()) 
                        .withStaticPart(joinpoint.getStaticPart()) );
        }
    }
}
