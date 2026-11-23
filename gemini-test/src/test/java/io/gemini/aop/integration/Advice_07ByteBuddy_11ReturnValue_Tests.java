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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Tests return value binding and assigning of ByteBuddy advice via {@link net.bytebuddy.asm.Advice}
 * and {@link net.bytebuddy.asm.Advice.AssignReturned} annotations.
 *
 * @author   martin.liu
 */
public class Advice_07ByteBuddy_11ReturnValue_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();
        Object exptected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(exptected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(exptected);
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_07ByteBuddy_11ReturnValue_Tests$TypeInitializer_Object)";


        static {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }

        static final Logger LOGGER;

        static {
            LOGGER = LoggerFactory.getLogger(TypeInitializer_Object.class);
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_01Binding_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_02Assigning_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Object after() {
            Object returnValue = 3;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_03Assigning_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned
        public static Object after() {
            Object returnValue = 4;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_04Binding_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testClassMethod() {
        int returnValue = ClassMethod_Object.returnValue();
        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(ClassMethod_02Assigning_Advisor.AFTER_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(
                    (int) ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD).getReturnValue() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static int io.gemini.aop.integration.Advice_07ByteBuddy_11ReturnValue_Tests$ClassMethod_Object.returnValue())";


        public static int returnValue() {
            int returnValue = 100;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_01Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_02Assigning_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int after() {
            int returnValue = 3;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_03Assigning_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int after() {
            int returnValue = 4;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object expected = new InstanceConstructor_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(io.gemini.aop.integration.Advice_07ByteBuddy_11ReturnValue_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }

        public InstanceConstructor_Object(boolean flag) {
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_01Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) InstanceConstructor_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
  public static class InstanceConstructor_02Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static InstanceConstructor_Object after() {
            InstanceConstructor_Object returnValue = new InstanceConstructor_Object(true);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_03Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static InstanceConstructor_Object after() {
            InstanceConstructor_Object returnValue = new InstanceConstructor_Object(true);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_04Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) InstanceConstructor_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testInstanceMethod() {
        int returnValue = new InstanceMethod_Object().returnValue();
        int expected = (int) ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_02Assigning_Advisor.AFTER_ADVICE)
                .getReturnValue();

        assertThat(returnValue).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD).getReturnValue() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_11IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_13IllegalAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_12IllegalBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetReturnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public int io.gemini.aop.integration.Advice_07ByteBuddy_11ReturnValue_Tests$InstanceMethod_Object.returnValue())";


        public int returnValue() {
            int returnValue = 100;

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_01Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_02Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int after() {
            int returnValue = 3;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_03Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int after() {
            int returnValue = 4;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_11IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_11IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_12IllegalBinding_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_12IllegalBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) int returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_13IllegalAssigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_13IllegalAssigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false, skipOn = Advice.OnNonDefaultValue.class)
        @Advice.AssignReturned.ToReturned(typing = Assigner.Typing.DYNAMIC)
        public static int before() {
            int returnValue = 1;

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }
}
