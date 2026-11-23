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

import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;

/**
 * Tests arguments accessing or modifying of target method.
 *
 * @author   martin.liu
 */
public class Advice_06AspectJ_03Arguments_Tests {

    private final static Logger LOGGER = LoggerFactory.getLogger(Advice_06AspectJ_03Arguments_Tests.class);


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
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEmpty();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEmpty();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEmpty();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEmpty();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$TypeInitializer_Object) && args()";


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

        @SuppressWarnings("rawtypes")
        @Before(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts() );
        }


        private static final String AFTER_ADVICE = TypeInitializer_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts() );
        }
    }

    @Aspect
    public static class TypeInitializer_RP_Aspect {

        @Pointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void arguments() {}


        private static final String BEFORE_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments()")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts() );
        }


        private static final String AFTER_ADVICE = TypeInitializer_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments()")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts() );
        }
    }


    @Test
    public void testClassMethod() {
        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        ClassMethod_Object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TAGRET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);

        }
    }

    public static class ClassMethod_Object {

        private static final String TAGRET_METHOD = ClassMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$ClassMethod_Object.arguments(..)) && args(arg, arg2)";


        public static List<Long> arguments(final long arg, final List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TAGRET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @Aspect
    public static class ClassMethod_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings({ "rawtypes" })
        @Before(value = ClassMethod_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = ClassMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = ClassMethod_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class ClassMethod_RP_Aspect {

        @Pointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String BEFORE_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String AFTER_ADVICE = ClassMethod_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        new InstanceConstructor_Object(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TAGRET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TAGRET_METHOD = InstanceConstructor_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$InstanceConstructor_Object.new(..)) && args(arg, arg2)";


        public InstanceConstructor_Object(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TAGRET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class InstanceConstructor_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = InstanceConstructor_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = InstanceConstructor_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class InstanceConstructor_RP_Aspect {

        @Pointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String BEFORE_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String AFTER_ADVICE = InstanceConstructor_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object object = new InstanceMethod_Object();

        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$InstanceMethod_Object.arguments(..)) && args(arg, arg2)";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @Aspect
    public static class InstanceMethod_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = InstanceMethod_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = InstanceMethod_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = InstanceMethod_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class InstanceMethod_RP_Aspect {

        @Pointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String BEFORE_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String AFTER_ADVICE = InstanceMethod_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg, arg2)", argNames = "arg, arg2")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testDisorderedArgs() {
        DisorderedArgs_Object object = new DisorderedArgs_Object();

        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(DisorderedArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisorderedArgs_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisorderedArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisorderedArgs_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisorderedArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class DisorderedArgs_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$DisorderedArgs_Object.arguments(..)) && args(arg, arg2)";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @Aspect
    public static class DisorderedArgs_Aspect {

        private static final String BEFORE_ADVICE = DisorderedArgs_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = DisorderedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg2, arg")
        public void before(MutableJoinpoint joinpoint, List<Long> arg2, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = DisorderedArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = DisorderedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg2, arg")
        public void after(MutableJoinpoint joinpoint, List<Long> arg2, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class DisorderedArgs_RP_Aspect {

        @Pointcut(DisorderedArgs_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String BEFORE_ADVICE = DisorderedArgs_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg, arg2)", argNames = "arg2, arg")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, List<Long> arg2, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(DisorderedArgs_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String AFTER_ADVICE = DisorderedArgs_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg, arg2)", argNames = "arg2, arg")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, List<Long> arg2, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testPartialArgs() {
        PartialArgs_Object object = new PartialArgs_Object();

        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(PartialArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

    }

    public static class PartialArgs_Object {

        private static final String TARGET_METHOD = PartialArgs_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$PartialArgs_Object.arguments(..)) && args(arg, ..)";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
            return arg2;
        }
    }

    @Aspect
    public static class PartialArgs_Aspect {

        private static final String BEFORE_ADVICE = PartialArgs_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = PartialArgs_Object.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(MutableJoinpoint joinpoint, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }


        private static final String AFTER_ADVICE = PartialArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = PartialArgs_Object.POINTCUT_EXPRESSION, argNames = "arg")
        public void after(MutableJoinpoint joinpoint, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class PartialArgs_RP_Aspect {

        @Pointcut(PartialArgs_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg) {
            LOGGER.info("arguments: {}", arg);
        }

        private static final String BEFORE_ADVICE = PartialArgs_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }


        @Pointcut(PartialArgs_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg) {
            LOGGER.info("arguments: {}", arg);
        }

        private static final String AFTER_ADVICE = PartialArgs_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg)", argNames = "arg")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }


    @Test
    public void testInconsistentArgs() {
        InconsistentArgs_Object object = new InconsistentArgs_Object();

        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InconsistentArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InconsistentArgs_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InconsistentArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InconsistentArgs_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InconsistentArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

    }

    public static class InconsistentArgs_Object {

        private static final String TARGET_METHOD = InconsistentArgs_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$InconsistentArgs_Object.arguments(..)) && args(arg, arg2)";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @Aspect
    public static class InconsistentArgs_Aspect {

        private static final String BEFORE_ADVICE = InconsistentArgs_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = InconsistentArgs_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2, arg3")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2, String arg3) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = InconsistentArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = InconsistentArgs_Object.POINTCUT_EXPRESSION, argNames = "arg, arg2, arg3")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2, String arg3) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class InconsistentArgs_RP_Aspect {

        @Pointcut(InconsistentArgs_Object.POINTCUT_EXPRESSION)
        private void arguments(final long arg, final List<Long> arg2, String arg3) {
            LOGGER.info("arguments: {}, {}, {}", arg, arg2, arg3);
        }

        private static final String BEFORE_ADVICE = InconsistentArgs_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg, arg2, arg3)", argNames = "arg, arg2, arg3")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2, String arg3) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(InconsistentArgs_Object.POINTCUT_EXPRESSION)
        private void arguments2(final long arg, final List<Long> arg2, String arg3) {
            LOGGER.info("arguments: {}, {}, {}", arg, arg2, arg3);
        }

        private static final String AFTER_ADVICE = InconsistentArgs_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg, arg2, arg3)", argNames = "arg, arg2, arg3")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2, String arg3) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testRenamedArgs() {
        RenamedArgs_Object object = new RenamedArgs_Object();

        long arg = 0;
        List<Long> arg2 = Lists.list(1L, 2L, 3L);
        object.arguments(arg, arg2);

        Object[] expected = new Object[] {arg, arg2};

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(RenamedArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(RenamedArgs_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class RenamedArgs_Object {

        private static final String TARGET_METHOD = RenamedArgs_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_06AspectJ_03Arguments_Tests$RenamedArgs_Object.arguments(..)) && args(arg_1, arg_2)";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @Aspect
    public static class RenamedArgs_Aspect {

        private static final String BEFORE_ADVICE = RenamedArgs_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg_1, arg_2")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        private static final String AFTER_ADVICE = RenamedArgs_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg_1, arg_2")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @Aspect
    public static class RenamedArgs_RP_Aspect {

        @Pointcut(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg_1, arg_2")
        private void arguments(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String BEFORE_ADVICE = RenamedArgs_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg_1, arg_2)", argNames = "arg_1, arg_2")
        @SuppressWarnings("rawtypes")
        public void before(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }


        @Pointcut(value = RenamedArgs_Object.POINTCUT_EXPRESSION, argNames = "arg_1, arg_2")
        private void arguments2(final long arg, final List<Long> arg2) {
            LOGGER.info("arguments: {}, {}", arg, arg2);
        }

        private static final String AFTER_ADVICE = RenamedArgs_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg_1, arg_2)", argNames = "arg_1, arg_2")
        @SuppressWarnings("rawtypes")
        public void after(MutableJoinpoint joinpoint, long arg, List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );
        }
    }
}
