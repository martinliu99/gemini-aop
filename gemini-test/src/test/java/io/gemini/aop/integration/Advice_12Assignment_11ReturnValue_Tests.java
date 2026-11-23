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
/**
 * 
 */
package io.gemini.aop.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.integration.Advice_12Assignment_02TargetObject_Tests.TargetObject_Base_Object;
import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
public class Advice_12Assignment_11ReturnValue_Tests {

    @Test
    public void testUpcastedType() {
        Object modified = new ReturnValue_Super_Object().returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_Super_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_Super_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01UpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02UpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01UpcastedTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_Super_Object.returnValue(..))";


        public ReturnValue_Super_Object returnValue() {
            ReturnValue_Super_Object returnValue = new ReturnValue_Super_Object();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    public static class ReturnValue_Sub_Object extends ReturnValue_Super_Object { }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01UpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(new ReturnValue_Sub_Object());

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_11IllegalUpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01UpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01UpcastedTypeBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_Super_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(TargetObject_Base_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02UpcastedTypeBinding_RP_Aspect {

        @Pointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02UpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(TargetObject_Base_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_11IllegalUpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_Super_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(String returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect {

        @Pointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
        private void returnValue(String returnValue) {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue(returnValue)", argNames = "returnValue", returning = "returnValue")
        public void after(String returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01UpcastedTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01UpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return TargetObject_Base_Object returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02UpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static ReturnValue_Super_Object after(
                @Advice.Return ReturnValue_Super_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return String returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_Super_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static String after(
                @Advice.Return Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return "1";
        }
    }


    @Test
    public void testVoidType() {
        new ReturnValue_VoidType_Object().returnValue();

        Object expected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_VoidType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_02VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_03VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01VoidTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_02VoidTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_03VoidTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_04VoidTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(0);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01VoidTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02VoidTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_03VoidTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_04VoidTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(0);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_05VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_06VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_07VoidTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    public static class ReturnValue_VoidType_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_VoidType_Object.returnValue(..))";


        public void returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(null) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_Pojo_01VoidTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01VoidTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            Object returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_Pojo_02VoidTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_02VoidTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            Long returnValue = 1L;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_Pojo_03VoidTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_03VoidTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            int returnValue = 1;
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01VoidTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01VoidTypeBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_VoidType_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Void returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_02VoidTypeBinding_RP_Aspect {

        @Pointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_02VoidTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_03VoidTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_03VoidTypeBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_VoidType_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Long returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_04VoidTypeBinding_RP_Aspect {

        @Pointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_AspectJ_04VoidTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01VoidTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01VoidTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Void returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02VoidTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02VoidTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_03VoidTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_03VoidTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Long returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_04VoidTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_04VoidTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_05VoidTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_05VoidTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static Object after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return new Object();
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_06VoidTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_06VoidTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static Long after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Long returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return 1L;
        }
    }

    @ExprPointcut(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_07VoidTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_07VoidTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return 1;
        }
    }


    @Test
    public void testBoxedPrimitive() {
        int modified = new ReturnValue_BoxedPrimitive_Object().returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_BoxedPrimitive_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_BoxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01BoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01BoxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_03BoxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01BoxedPrimitiveBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02BoxedPrimitiveBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_11IllegalBoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_12IllegalBoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_BoxedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_BoxedPrimitive_Object.returnValue(..))";


        public int returnValue() {
            int returnValue = 1;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01BoxedPrimitiveAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01BoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(-1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01BoxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01BoxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Integer returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Integer returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_03BoxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_03BoxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01BoxedPrimitiveBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01BoxedPrimitiveBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return Integer returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02BoxedPrimitiveBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02BoxedPrimitiveBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return Number returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_11IllegalBoxedPrimitiveAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_11IllegalBoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Integer after(
                @Advice.Return Integer returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ReturnValue_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_12IllegalBoxedPrimitiveAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_12IllegalBoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Long after(
                @Advice.Return Integer returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return 1L;
        }
    }


    @Test
    public void testUnboxedPrimitive() {
        Integer modified = new ReturnValue_UnboxedPrimitive_Object().returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_UnboxedPrimitive_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_UnboxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01UnboxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01UnboxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01UnboxedPrimitiveBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_11IllegalBoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class ReturnValue_UnboxedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_UnboxedPrimitive_Object.returnValue(..))";


        public Integer returnValue() {
            Integer returnValue = 1;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01UnboxedPrimitiveAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(-1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01UnboxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01UnboxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Integer returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Integer returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Aspect
    public static class ReturnValue_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01UnboxedPrimitiveBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01UnboxedPrimitiveBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return Integer returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Integer after(
                @Advice.Return Integer returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static int after(
                @Advice.Return Integer returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ReturnValue_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_11IllegalUnboxedPrimitiveBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_11IllegalUnboxedPrimitiveBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return int returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testWidenedPrimitive() {
        int modified = new ReturnValue_WidenedPrimitive_Object().returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_WidenedPrimitive_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_WidenedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01WidenedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01WidenedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01WidenedPrimitiveBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02WidenedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }
    }

    public static class ReturnValue_WidenedPrimitive_Object {

        public static final String TARGET_METHOD = ReturnValue_Super_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_WidenedPrimitive_Object.returnValue(..))";


        public int returnValue() {
            Integer returnValue = 1;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01WidenedPrimitiveAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01WidenedPrimitiveAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(-1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01WidenedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01WidenedPrimitiveBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_WidenedPrimitive_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(long returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect {

        @Pointcut(ReturnValue_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(long returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01WidenedPrimitiveBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01WidenedPrimitiveBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return long returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02WidenedPrimitiveAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02WidenedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static short after(
                @Advice.Return int returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return (short) returnValue;
        }
    }


    @Test
    public void testGenericType() {
        int modified = new ReturnValue_GenericType_Object<Integer>(1).returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_GenericType_Object.TARGET_METHOD)
                .getReturnValue();
        assertThat(modified).isNotEqualTo(origin);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01GenericTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01GenericTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02GenericTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01GenericTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02GenericTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }
    }

    public static class ReturnValue_GenericType_Object<T extends Number & Comparable<T>> {

        public static final String TARGET_METHOD = ReturnValue_GenericType_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_GenericType_Object.returnValue(..))";

        private final T initialValue;

        public ReturnValue_GenericType_Object(T initialValue) {
            this.initialValue = initialValue;
        }

        public T returnValue() {
            T returnValue = initialValue;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01GenericTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01GenericTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(-1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01GenericTypeBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01GenericTypeBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_GenericType_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02GenericTypeBinding_RP_Aspect {

        @Pointcut(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02GenericTypeBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Number returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01GenericTypeBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01GenericTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return Number returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue((int) returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02GenericTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02GenericTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Number after(
                @Advice.Return Number returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }


    @Test
    public void testGenericArray() {
        Integer[][] modified = new ReturnValue_GenericArray_Object<Integer>(new Integer[][] {{1}}).returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_GenericArray_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericArray_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01GenericArrayAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01GenericArrayBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02GenericArrayBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01GenericArrayBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02GenericArrayAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }
    }

    public static class ReturnValue_GenericArray_Object<T extends Number & Comparable<T>> {

        public static final String TARGET_METHOD = ReturnValue_GenericArray_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_GenericArray_Object"
                + ".returnValue(..))";


        private final T[][] initialValue;

        public ReturnValue_GenericArray_Object(T[][] initialValue) {
            this.initialValue = initialValue;
        }

        public T[][] returnValue() {
            T[][] returnValue = initialValue;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01GenericArrayAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01GenericArrayAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(new Integer[][] {{-1}});

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01GenericArrayBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01GenericArrayBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(Number[][] returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02GenericArrayBinding_RP_Aspect {

        @Pointcut(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02GenericArrayBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(Number[][] returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01GenericArrayBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01GenericArrayBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return Number[][] returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02GenericArrayAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02GenericArrayAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static Number[][] after(
                @Advice.Return Number[][] returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }


    @Test
    public void testParameterizedCollection() {
        List<Integer> modified = new ReturnValue_ParameterizedCollection_Object()
                .returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_ParameterizedCollection_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_ParameterizedCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01ParameterizedCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01ParameterizedCollectionBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01ParameterizedCollectionBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02ParameterizedCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }
    }


    static class ReturnValue_ParameterizedCollection_Object {

        public static final String TARGET_METHOD = ReturnValue_ParameterizedCollection_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_ParameterizedCollection_Object"
                + ".returnValue())";


        public List<Integer> returnValue() {
            List<Integer> returnValue = new ArrayList<>();
            returnValue.add(1);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01ParameterizedCollectionAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01ParameterizedCollectionAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ArrayList<Object> returnValue = new ArrayList<>();
            returnValue.add(-1);
            joinpoint.setReturnValue(returnValue);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01ParameterizedCollectionBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01ParameterizedCollectionBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_ParameterizedCollection_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(List<Integer> returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
            returnValue.add(1);
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect {

        @Pointcut(ReturnValue_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(List<Integer> returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01ParameterizedCollectionBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01ParameterizedCollectionBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return List<Integer> returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02ParameterizedCollectionAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02ParameterizedCollectionAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static List<Integer> after(
                @Advice.Return List<Integer> returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }


    @Test
    public void testWildCardCollection() {
        List<? extends Number> modified = new ReturnValue_WildCardCollection_Object(Arrays.asList(1, 2, 3))
                .returnValue();

        Object origin = ExecutionMemento
                .getTargetMethodInvoker(ReturnValue_WildCardCollection_Object.TARGET_METHOD)
                .getReturnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_WildCardCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_01Pojo_01WildCardCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_01WildCardCollectionBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_02AspectJ_02WildCardCollectionBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_01WildCardCollectionBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_ByteBuddy_02WildCardCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(modified);
        }
    }

    static class ReturnValue_WildCardCollection_Object {

        public static final String TARGET_METHOD = ReturnValue_GenericArray_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_11ReturnValue_Tests$ReturnValue_WildCardCollection_Object"
                + ".returnValue())";

        private List<? extends Number> counters;


        public ReturnValue_WildCardCollection_Object(List<? extends Number> counters) {
            this.counters = counters;
        }

        public List<? extends Number> returnValue() {
            ArrayList<? extends Number> returnValue = new ArrayList<>(counters);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(ReturnValue_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_01Pojo_01WildCardCollectionAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractAfter {

        private static final String AFTER_ADVICE = ReturnValue_01Pojo_01WildCardCollectionAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setReturnValue(new ArrayList<>());

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_01WildCardCollectionBinding_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_01WildCardCollectionBinding_Aspect.class.getName() + ".after";

        @AfterReturning(value = ReturnValue_WildCardCollection_Object.POINTCUT_EXPRESSION, argNames = "returnValue", returning = "returnValue")
        public void after(List<? extends Number> returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @Aspect
    public static class ReturnValue_02AspectJ_02WildCardCollectionBinding_RP_Aspect {

        @Pointcut(ReturnValue_WildCardCollection_Object.POINTCUT_EXPRESSION)
        private void returnValue() {
        }


        private static final String AFTER_ADVICE = ReturnValue_02AspectJ_02WildCardCollectionBinding_RP_Aspect.class.getName() + ".after";

        @AfterReturning(value = "returnValue()", argNames = "returnValue", returning = "returnValue")
        public void after(List<? extends Number> returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_01WildCardCollectionBinding_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_01WildCardCollectionBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return List<? extends Number> returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValue_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class ReturnValue_ByteBuddy_02WildCardCollectionAssigning_Advisor {

        private static final String AFTER_ADVICE = ReturnValue_ByteBuddy_02WildCardCollectionAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static List<? extends Number> after(
                @Advice.Return List<? extends Number> returnValue
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
            return returnValue;
        }
    }
}
