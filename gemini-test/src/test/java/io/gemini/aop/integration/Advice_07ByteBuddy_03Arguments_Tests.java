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
import java.util.List;

import org.assertj.core.util.Lists;
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
 * Tests target arguments binding and assigning of ByteBuddy advice via {@link net.bytebuddy.asm.Advice}
 * and {@link net.bytebuddy.asm.Advice.AssignReturned} annotations.
 * 
 * @author   martin.liu
 */
public class Advice_07ByteBuddy_03Arguments_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isNotEmpty();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_02Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEmpty();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$TypeInitializer_Object)";


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
    public static class TypeInitializer_01Assigning_Advisor {

        private static final String BEFORE_ADVICE = TypeInitializer_01Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToAllArguments(typing = Assigner.Typing.DYNAMIC)
        public static Object[] before() {
            Object[] arguments = new Object[1];

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );

            return arguments;
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_02Binding_Advisor {

        private static final String AFTER_ADVICE = TypeInitializer_02Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.AllArguments Object[] arguments) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );
        }
    }


    @Test
    public void testClassMethod() {
        long l = 0L;
        List<Long> input = Lists.list();

        ClassMethod_Object.arguments(l, input);

        {
            Object[] expected = newArguments(3L);

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            // backup argument
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_02Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_03Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(3L);

            // modify argument slot
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    private Object[] newArguments(long l) {
        List<Long> input = new ArrayList<>();
        input.add(0, l);
        return new Object[] {l, input};
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static * io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$ClassMethod_Object.arguments(..))";


        public static List<Long> arguments(long arg, List<Long> arg2) {
            arg = 3L;
            arg2 = new ArrayList<Long>();
            arg2.add(arg);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_01Assigning_Advisor {

        private static final String BEFORE_ADVICE = ClassMethod_01Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToAllArguments(typing = Assigner.Typing.DYNAMIC)
        public static Object[] before() {
            Object[] arguments = new Object[2];

            long arg = 1L;
            arguments[0] = arg;

            List<Long> arg2 = new ArrayList<>();
            arg2.add(0, arg);
            arguments[1] = arg2;

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );

            return arguments;
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_02Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_02Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.AllArguments Object[] arguments) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_03Assigning_Advisor {

        private static final String BEFORE_ADVICE = ClassMethod_03Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, index = 0, typing = Assigner.Typing.DYNAMIC),
            @Advice.AssignReturned.ToArguments.ToArgument(value = 1, index = 1, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Object[] before() {
            long arg = 2L;
            List<Long> arg2 = new ArrayList<>();
            arg2.add(0, arg);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );

            return new Object[] {arg, arg2};
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.Argument(0) long arg, 
                @Advice.Argument(1) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg, arg2) );
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

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            // backup argument
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(3L);

            // modify argument slot
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$InstanceConstructor_Object.new(..))";


        public InstanceConstructor_Object(long arg, List<Long> arg2) {
            arg = 3L;
            arg2 = new ArrayList<>();
            arg2.add(arg);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_01Assigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToAllArguments(typing = Assigner.Typing.DYNAMIC)
        public static Object[] before() {
            Object[] arguments = new Object[2];

            long arg = 1L;
            arguments[0] = arg;

            List<Long> arg2 = new ArrayList<>();
            arg2.add(0, arg);
            arguments[1] = arg2;

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );

            return arguments;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_02Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_01Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = true)
        public static void after(
                @Advice.AllArguments Object[] arguments) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_03Assigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceConstructor_03Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, index = 0, typing = Assigner.Typing.DYNAMIC),
            @Advice.AssignReturned.ToArguments.ToArgument(value = 1, index = 1, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Object[] before() {
            long arg = 2L;
            List<Long> arg2 = new ArrayList<>();
            arg2.add(0, arg);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );

            return new Object[] {arg, arg2};
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_04Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_03Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.Argument(0) long arg, 
                @Advice.Argument(1) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testInstanceMethod() {
        long arg = 0L;
        List<Long> arg2 = Lists.list();

        new InstanceMethod_Object().arguments(arg, arg2);

        {
            Object[] expected = newArguments(3L);

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(1L);

            // backup arguments
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(2L);

            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_03Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            Object[] expected = newArguments(3L);

            // modify argument slot
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_04Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_11IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_12IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".arguments";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$InstanceMethod_Object.arguments(..))";


        public List<Long> arguments(long arg, List<Long> arg2) {
            arg = 3L;
            arg2 = new ArrayList<>();
            arg2.add(arg);

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_01Assigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToAllArguments(typing = Assigner.Typing.DYNAMIC)
        public static Object[] before() {
            Object[] arguments = new Object[2];

            long arg = 1L;
            arguments[0] = arg;

            List<Long> arg2 = new ArrayList<>();
            arg2 = new ArrayList<>(arg2);
            arg2.add(0, arg);
            arguments[1] = arg2;

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );

            return arguments;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_02Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_02Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arguments) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_03Assigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_03Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, index = 0, typing = Assigner.Typing.DYNAMIC),
            @Advice.AssignReturned.ToArguments.ToArgument(value = 1, index = 1, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Object[] before() {
            long arg = 2L;
            List<Long> arg2 = new ArrayList<>();
            arg2.add(0, arg);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );

            return new Object[] {arg, arg2};
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_04Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_04Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.Argument(0) long arg, 
                @Advice.Argument(1) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg, arg2) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_11IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_11IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arguments) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_12IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_12IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.Argument(value = 0, readOnly = false) long arg, 
                @Advice.Argument(value = 1, readOnly = false) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg, arg2) );
        }
    }

    @Test
    public void testDisOrderArgs() {
        long arg = 0L;
        List<Long> arg2 = Lists.list(arg);

        new DisOrderArgs_Object().arguments(arg, arg2);
        Object[] expected = newArguments(0L);

        {

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(DisOrderArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisOrderArgs_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(DisOrderArgs_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class DisOrderArgs_Object {

        private static final String TARGET_METHOD = DisOrderArgs_Object.class.getName() + ".arguments";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2) );
            return arg2;
        }
    }

    @ExprPointcut("execution(public * io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$DisOrderArgs_Object.arguments(..))")
    public static class DisOrderArgs_Advisor {

        private static final String BEFORE_ADVICE = DisOrderArgs_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, index = 1, typing = Assigner.Typing.DYNAMIC),
            @Advice.AssignReturned.ToArguments.ToArgument(value = 1, index = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Object[] before(
                @Advice.Argument(1) List<Long> arg2,
                @Advice.Argument(0) long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg, arg2) );

            return new Object[] {arg2, arg};
        }


        private static final String AFTER_ADVICE = DisOrderArgs_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Argument(1) List<Long> arg2,
                @Advice.Argument(0) long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg, arg2) );
        }
    }


    @Test
    public void testPartialArgs() {
        long arg = 0L;
        List<Long> arg2 = Lists.list(arg);

        new PartialArgs_Object().arguments(arg, arg2);
        Object expected = new Object[] {arg2};

        {

            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(PartialArgs_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(PartialArgs_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(expected);
        }
    }

    public static class PartialArgs_Object {

        private static final String TARGET_METHOD = PartialArgs_Object.class.getName() + ".arguments";


        public List<Long> arguments(long arg, List<Long> arg2) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg2) );
            return arg2;
        }
    }

    @ExprPointcut("execution(public * io.gemini.aop.integration.Advice_07ByteBuddy_03Arguments_Tests$PartialArgs_Object.arguments(..))")
    public static class PartialArgs_Advisor {

        private static final String BEFORE_ADVICE = DisOrderArgs_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 1, index = -1, typing = Assigner.Typing.DYNAMIC)
        } )
        public static List<Long> before(
                @Advice.Argument(1) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg2) );

            return arg2;
        }


        private static final String AFTER_ADVICE = DisOrderArgs_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Argument(1) List<Long> arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withArgumnts(arg2) );
        }
    }
}
