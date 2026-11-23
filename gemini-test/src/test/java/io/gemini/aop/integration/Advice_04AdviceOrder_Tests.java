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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.annotation.Order;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ConditionalOnClassLoader;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;

/**
 * Tests advice class order.
 * 
 * @author   martin.liu
 */
public class Advice_04AdviceOrder_Tests {

    private static final String POINTCUT_EXPRESSION = 
            "execution(public void io.gemini.aop.integration.Advice_04AdviceOrder_Tests$AdviceOrder_Object.match())"
            + " || "
            + "execution(private native static long java.util.zip.Deflater.init(int, int, boolean))";

    private static final List<String> EXPECTED_ADVICE_METHODS = Lists.list(
            AdviceOrder_ByteBuddy_1_Advisor.BEFORE_ADVICE,

            AdviceOrder_ByteBuddy_2_Advisor.BEFORE_ADVICE,

            AdviceOrder_01Pojo_Advisor.BEFORE_ADVICE,

            AdviceOrder_02AspectJ_Aspect.BEFORE_ADVICE,

            AdviceOrder_02AspectJ_Aspect.AFTER_ADVICE,

            AdviceOrder_01Pojo_Advisor.AFTER_ADVICE,

            AdviceOrder_ByteBuddy_2_Advisor.AFTER_ADVICE,

            AdviceOrder_ByteBuddy_1_Advisor.AFTER_ADVICE
        );


    @Test
    public void testAdviceOrder_regularMethod() {
        // regular method
        new AdviceOrder_Object().match();

        List<String> advicemethodNames = new ArrayList<>(ExecutionMemento.getAdviceMethodNames());
        advicemethodNames.retainAll(EXPECTED_ADVICE_METHODS);
        assertThat(advicemethodNames).isEqualTo(EXPECTED_ADVICE_METHODS);
    }

    @Test
    public void testAdviceOrder_nativeMethod() {
        // native method
        byte[] data = "This is a long text that needs compression".getBytes(Charset.defaultCharset());

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        deflater.end();

        List<String> advicemethodNames = new ArrayList<>(ExecutionMemento.getAdviceMethodNames());
        advicemethodNames.retainAll(EXPECTED_ADVICE_METHODS);
        assertThat(advicemethodNames).isEqualTo(EXPECTED_ADVICE_METHODS);
    }

    public static class AdviceOrder_Object {

        public void match() {  }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(POINTCUT_EXPRESSION)
    @ConditionalOnClassLoader(isBootstrapClassLoader = true, isAppClassLoader = true)
    private static class AdviceOrder_01Pojo_Advisor extends Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = AdviceOrder_01Pojo_Advisor.class.getName() + ".before";

        /**
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, new AdviceMethod());
        }


        private static final String AFTER_ADVICE = AdviceOrder_01Pojo_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, new AdviceMethod());
        }
    }

    @Aspect
    @ConditionalOnClassLoader(isBootstrapClassLoader = true, isAppClassLoader = true)
    protected static class AdviceOrder_02AspectJ_Aspect {

        private static final String BEFORE_ADVICE = AdviceOrder_02AspectJ_Aspect.class.getName() + ".before";

        @Before(POINTCUT_EXPRESSION)
        public void before() {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, new AdviceMethod());
        }


        private static final String AFTER_ADVICE = AdviceOrder_02AspectJ_Aspect.class.getName() + ".after";

        @After(POINTCUT_EXPRESSION)
        public static void after() {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, new AdviceMethod());
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    @ConditionalOnClassLoader(isBootstrapClassLoader = true, isAppClassLoader = true)
    @Order(20)
    static class AdviceOrder_ByteBuddy_1_Advisor {

        private static final String BEFORE_ADVICE = AdviceOrder_ByteBuddy_1_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, new AdviceMethod());
        }


        private static final String AFTER_ADVICE = AdviceOrder_ByteBuddy_1_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, new AdviceMethod());
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    @ConditionalOnClassLoader(isBootstrapClassLoader = true, isAppClassLoader = true)
    @Order(21)
    static class AdviceOrder_ByteBuddy_2_Advisor {

        private static final String BEFORE_ADVICE = AdviceOrder_ByteBuddy_2_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, new AdviceMethod());
        }


        private static final String AFTER_ADVICE = AdviceOrder_ByteBuddy_2_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after() throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, new AdviceMethod());
        }
    }
}