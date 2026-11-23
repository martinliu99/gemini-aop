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

import java.io.FileNotFoundException;
import java.io.IOException;

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
 * Tests thrown exception value binding and assigning of ByteBuddy advice via {@link net.bytebuddy.asm.Advice}
 * and {@link net.bytebuddy.asm.Advice.AssignReturned} annotations.
 *
 * @author   martin.liu
 */
public class Advice_07ByteBuddy_12ThrownException_Tests {

    @Test
    public void testTypeInitializer() {
        Throwable thrownException = null;
        try {
            new TypeInitializer_Object();

            assertThat(false).isTrue();
        } catch (ExceptionInInitializerError e) {
            thrownException = e.getCause();
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(TypeInitializer_02Assigning_Advisor.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }
    }


    private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targeThrownException";

    private static final String POINTCUT_EXPRESSION = 
            "staticinitialization(io.gemini.aop.integration.Advice_07ByteBuddy_12ThrownException_Tests$TypeInitializer_Object)";

    public static class TypeInitializer_Object {

        static {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            int i = 0;
            if (i ==0)
                throw thrownException;
        }

        static final Logger LOGGER;

        static {
            LOGGER = LoggerFactory.getLogger(TypeInitializer_Object.class);
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    public static class TypeInitializer_01Binding_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    public static class TypeInitializer_02Assigning_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static RuntimeException after() {
            RuntimeException thrownException = new ClassCastException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    public static class TypeInitializer_03Assigning_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static RuntimeException after() {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(POINTCUT_EXPRESSION)
    public static class TypeInitializer_04Binding_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testClassMethod() {
        Throwable thrownException = null;
        try {
            ClassMethod_Object.thrownException();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(ClassMethod_02Assigning_Advisor.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targeThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static void io.gemini.aop.integration.Advice_07ByteBuddy_12ThrownException_Tests$ClassMethod_Object.thrownException())";


        public static void thrownException() throws IOException {
            IOException thrownException  = new IOException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_01Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_02Assigning_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static IOException after() {
            IOException thrownException = new FileNotFoundException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_03Assigning_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static RuntimeException after() {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testInstanceConstructor() {
        Throwable thrownException = null;
        try {
            new InstanceConstructor_Object();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isInstanceOf(IOException.class);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isEqualTo(thrownException);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_13IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_12IllegalAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_11IllegalAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targeThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(io.gemini.aop.integration.Advice_07ByteBuddy_12ThrownException_Tests$InstanceConstructor_Object.new())";


        public InstanceConstructor_Object() throws IOException {
            IOException thrownException = new IOException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_11IllegalAssigning_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_11IllegalAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static Throwable after() {
            Exception thrownException = new Exception("");

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_12IllegalAssigning_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_12IllegalAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static Throwable after() {
            IOException thrownException = new IOException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_13IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_13IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) IOException thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testInstanceMethod() {
        Throwable thrownException = null;
        try {
            new InstanceMethod_Object().thrownException();

            assertThat(false).isTrue();
        } catch (Exception e) {
            thrownException = e;
        }

        Throwable expected = ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_02Assigning_Advisor.AFTER_ADVICE)
                .getThrownException();

        assertThat(thrownException).isNotNull();
        assertThat(thrownException).isEqualTo(expected);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(
                    ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD).getThrownException() );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isNotEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_13IllegalAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_12IllegalBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_11IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targeThrownException";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Advice_07ByteBuddy_12ThrownException_Tests$InstanceMethod_Object.thrownException())";


        public void thrownException() throws IOException {
            IOException thrownException = new IOException();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                    .withThrownException(thrownException) );

            throw thrownException;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_01Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_02Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static IOException after() {
            IOException thrownException = new IOException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_03Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static RuntimeException after() {
            RuntimeException thrownException = new RuntimeException();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_11IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_11IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, onThrowable = Throwable.class)
        public static void after(
                @Advice.Thrown(readOnly = false, typing = Assigner.Typing.DYNAMIC) Exception thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_12IllegalBinding_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_12IllegalBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Thrown(typing = Assigner.Typing.DYNAMIC) Throwable thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_13IllegalAssigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_13IllegalAssigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThrown(typing = Assigner.Typing.DYNAMIC)
        public static Throwable before() {
            Exception thrownException = new Exception("");

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );

            return thrownException;
        }
    }
}
