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

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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
public class MutableJoinpoint_03Arguments_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEmpty();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEmpty();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = "staticinitialization(io.gemini.aop.integration.MutableJoinpoint_03Arguments_Tests$TypeInitializer_Object)";


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
    public static class TypeInitializer_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = TypeInitializer_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(TypeInitializer_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            joinpoint.setArguments();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
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
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }


    @Test
    public void testClassMethod() {
        long l = 0L;
        List<Long> input = Lists.list();
        input.add(l);

        ClassMethod_Object.arguments(l, input);

        {
            Object[] expected = newArguments(3L);

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TAGRET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            // backup argument
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        } 
    }

    private Object[] newArguments(long l) {
        List<Long> input = new ArrayList<>();
        input.add(0, l);
        Object[] expected = new Object[] {l, input};
        return expected;
    }

    public static class ClassMethod_Object {

        private static final String TAGRET_METHOD = ClassMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.MutableJoinpoint_03Arguments_Tests$ClassMethod_Object.arguments(..))";


        public static List<Long> arguments(long l, List<Long> input) {
            l = 3L;
            input = new ArrayList<Long>();
            input.add(l);

            ExecutionMemento.putTargetMethodInvoker(TAGRET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(l, input) );
            return input;
        }
    }

    @Aspect
    public static class ClassMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 1L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
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
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class ClassMethod_03Assiging_Aspect {

        private static final String BEFORE_ADVICE = ClassMethod_03Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 2L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class ClassMethod_04Accessing_Aspect {

        private static final String AFTER_ADVICE = ClassMethod_04Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(ClassMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        long l = 0L;
        List<Long> input = Lists.list();

        new InstanceConstructor_Object(l, input);

        {
            Object[] expected = newArguments(3L);

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            // backup argument
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            // modify argument slot
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_04Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.MutableJoinpoint_03Arguments_Tests$InstanceConstructor_Object.new(long, java.util.List<java.lang.Long>))";


        public InstanceConstructor_Object(long l, List<Long> input) {
            l = 3L;
            input = new ArrayList<>();
            input.add(l);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(l, input) );
        }
    }

    @Aspect
    public static class InstanceConstructor_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 1L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_02Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_02Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_03Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceConstructor_03Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 2L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class InstanceConstructor_04Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceConstructor_04Accessing_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @After(InstanceConstructor_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }


    @Test
    public void testInstanceMethod() {
        List<Long> input = Lists.list();
        long l = 0L;

        new InstanceMethod_Object().arguments(l, input);

        {
            Object[] expected = newArguments(3L);

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            // backup arguments
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assiging_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            // modify argument slot
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Accessing_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.MutableJoinpoint_03Arguments_Tests$InstanceMethod_Object.arguments(..))";


        public List<Long> arguments(long l, List<Long> input) {
            l = 3L;
            input = new ArrayList<>();
            input.add(l);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(l, input) );
            return input;
        }
    }

    @Aspect
    public static class InstanceMethod_01Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 1L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
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
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class InstanceMethod_03Assiging_Aspect {

        private static final String BEFORE_ADVICE = InstanceMethod_03Assiging_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void before(MutableJoinpoint joinpoint) {
            long l = 2L;
            List<Long> input = new ArrayList<>();
            input.add(0, l);

            joinpoint.setArguments(l, input);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class InstanceMethod_04Accessing_Aspect {

        private static final String AFTER_ADVICE = InstanceMethod_04Accessing_Aspect.class.getName() + ".after";

                @SuppressWarnings("rawtypes")
        @After(InstanceMethod_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }
}
