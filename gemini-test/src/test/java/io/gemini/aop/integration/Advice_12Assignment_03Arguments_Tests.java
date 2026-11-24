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

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.integration.Advice_12Assignment_02TargetObject_Tests.TargetObject_Base_Object;
import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
@SuppressWarnings("unused")
public class Advice_12Assignment_03Arguments_Tests {

    @Test
    public void testUpcastedType() {
        Argument_Super_Object arg = new Argument_Super_Object();
        new Argument_Super_Object().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_Super_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_Super_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01UpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_12IllegalUpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_12IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02UpcastedTypeBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01UpcastedTypeBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02UpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_03UpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments().clone()[0]).isInstanceOf(Argument_Sub_Object.class);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_Super_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_Super_Object.arguments(..))";


        public void arguments(Argument_Super_Object arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    public static class Argument_Sub_Object extends Argument_Super_Object {
        
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01UpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(new Object[] {new Argument_Super_Object()});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_11IllegalUpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".before";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setArguments();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setArguments(new Object []{});

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_12IllegalUpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_12IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".before";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setArguments(new Object[] {joinpoint.getArguments()[0], 1});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_12IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setArguments(1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01UpcastedTypeBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_Super_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(TargetObject_Base_Object arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02UpcastedTypeBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(TargetObject_Base_Object arg) {
        }


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02UpcastedTypeBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(TargetObject_Base_Object arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_11IllegalUpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = Arguments_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.class.getName() + ".after";

        @After(value = Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void after(Long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Long arg) {
        }

        private static final String AFTER_ADVICE = Arguments_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments(arg)", argNames = "arg")
        public void after(Long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01UpcastedTypeBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01UpcastedTypeBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) TargetObject_Base_Object arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02UpcastedTypeAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Argument_Super_Object before(
                @Advice.Argument(0) Argument_Super_Object arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }

    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_03UpcastedTypeAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_03UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Argument_Sub_Object before() {
            Argument_Sub_Object arg = new Argument_Sub_Object();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }

    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor {

        private static final String AFTER_ADVICE = Arguments_ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.Argument(0) Long arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_Super_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = Arguments_ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Long after(
                @Advice.Argument(0) Number arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return 1L;
        }
    }


    @Test
    public void testBoxedPrimitive() {
        int arg = 1;
        new Argument_BoxedPrimitive_Object().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_BoxedPrimitive_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_BoxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01BoxedPrimitiveAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01BoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_03BoxedPrimitiveBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01BoxedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02BoxedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_03BoxedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_04IllegalBoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_05IllegalBoxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_BoxedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_BoxedPrimitive_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_BoxedPrimitive_Object.arguments(..))";


        public void arguments(int arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01BoxedPrimitiveAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01BoxedPrimitiveAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[] > ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(-1);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01BoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(Integer arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Integer arg) {
        }

        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02BoxedPrimitiveBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(Integer arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_03BoxedPrimitiveBinding_Aspect {

        private static final String BEFORE_ADVICE = Arguments_02AspectJ_03BoxedPrimitiveBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(Object arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Integer arg) {
        }

        private static final String BEFORE_ADVICE = Arguments_02AspectJ_04BoxedPrimitiveBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(Object arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = Arguments_02AspectJ_11IllegalBoxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @After(value = Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void after(Number arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01BoxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Number arg) {
        }

        private static final String AFTER_ADVICE = Arguments_02AspectJ_12IllegalBoxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments(arg)", argNames = "arg")
        public void after(Number arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01BoxedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01BoxedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Integer arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02BoxedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02BoxedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Number arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_03BoxedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_03BoxedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Object arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_04IllegalBoxedPrimitiveAssigning_Advisor {
 
        private static final String AFTER_ADVICE = Arguments_ByteBuddy_04IllegalBoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Integer after(
                @Advice.Argument(0) Integer arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }

    @ExprPointcut(Argument_BoxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_05IllegalBoxedPrimitiveAssigning_Advisor {
 
        private static final String AFTER_ADVICE = Arguments_ByteBuddy_05IllegalBoxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Object after(
                @Advice.Argument(0) Integer arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }


    @Test 
    public void testUnboxedPrimitive() {
        Integer arg = 1;
        new Argument_UnboxedPrimitive_Object().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_UnboxedPrimitive_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_UnboxedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01UnboxedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_11IllegalUnboxedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class Argument_UnboxedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_UnboxedPrimitive_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_UnboxedPrimitive_Object.arguments(..))";


        public void arguments(Integer arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01UnboxedPrimitiveAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01UnboxedPrimitiveAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(1);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(Integer arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Integer arg) {
        }

        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02UnboxedPrimitiveBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(Integer arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect {

        private static final String AFTER_ADVICE = Arguments_02AspectJ_11IllegalUnboxedPrimitiveBinding_Aspect.class.getName() + ".after";

        @After(value = Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void after(int arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01UnboxedPrimitiveBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments2(int arg) {
        }

        private static final String AFTER_ADVICE = Arguments_02AspectJ_12IllegalUnboxedPrimitiveBinding_RP_Aspect.class.getName() + ".after";

        @After(value = "arguments2(arg)", argNames = "arg")
        public void after(int arg) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01UnboxedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01UnboxedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Integer arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor {
 
        private static final String AFTER_ADVICE = Arguments_ByteBuddy_02UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Integer after(
                @Advice.Argument(0) Integer arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }

    @ExprPointcut(Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor {
 
        private static final String AFTER_ADVICE = Arguments_ByteBuddy_03UnboxedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static int after(
                @Advice.Argument(0) Integer arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }

    @ExprPointcut(Argument_UnboxedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_11IllegalUnboxedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_11IllegalUnboxedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) int arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }


    @Test
    public void testWidenedPrimitive() {
        int arg = 1;
        new Argument_WidenedPrimitive_Object().arguments(arg);

        Object[] origin = ExecutionMemento
                .getTargetMethodInvoker(Argument_WidenedPrimitive_Object.TARGET_METHOD)
                .getArguments();
        assertThat(new Object[] {arg}).isEqualTo(origin);

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_WidenedPrimitive_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01WidenedPrimitiveBindning_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01WidenedPrimitiveBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02WidenedPrimitiveAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }
    }

    public static class Argument_WidenedPrimitive_Object {

        public static final String TARGET_METHOD = Argument_WidenedPrimitive_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_WidenedPrimitive_Object.arguments(..))";


        public void arguments(int arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01WidenedPrimitiveBindning_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_WidenedPrimitive_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01WidenedPrimitiveBindning_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01WidenedPrimitiveBindning_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts((int)arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01WidenedPrimitiveBindning_Aspect.POINTCUT_EXPRESSION)
        private void arguments(long arg) {
        }

        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02WidenedPrimitiveBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(long arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts((int) arg) );
        }
    }

    @ExprPointcut(Argument_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01WidenedPrimitiveBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01WidenedPrimitiveBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) long arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts((int) arg) );
        }
    }

    @ExprPointcut(Argument_WidenedPrimitive_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02WidenedPrimitiveAssigning_Advisor {
 
        private static final String AFTER_ADVICE = Arguments_ByteBuddy_02WidenedPrimitiveAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static short after(
                @Advice.Argument(0) int arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return (short) arg;
        }
    }


    @Test
    public void testGenericType() {
        Integer arg = 1;
        new Argument_GenericType_Object<Integer>().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_GenericType_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01GenericTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01GenericTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01GenericTypeBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02GenericTypeBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01GenericTypeBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02GenericTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }
    }

    public static class Argument_GenericType_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_GenericType_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_GenericType_Object.arguments(..))";


        public void arguments(T arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_GenericType_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01GenericTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01GenericTypeAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(new Object[] {new Argument_GenericType_Object()});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01GenericTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01GenericTypeBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_GenericType_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01GenericTypeBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01GenericTypeBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(Number arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02GenericTypeBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01GenericTypeBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Number arg) {
        }


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02GenericTypeBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(Number arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_GenericType_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01GenericTypeBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01GenericTypeBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Number arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_GenericType_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02GenericTypeAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02GenericTypeAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Number before(
                @Advice.Argument(0) Number arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }


    @Test
    public void testGenericArray() {
        Integer[][] arg = new Integer[][] {};
        new Argument_GenericArray_Object<Integer>().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_GenericArray_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_GenericArray_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01GenericArrayAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01GenericArrayAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01GenericArrayBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02GenericArrayBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01GenericArrayBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02GenericArrayAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }
    }

    public static class Argument_GenericArray_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_GenericArray_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_GenericArray_Object.arguments(..))";


        public void arguments(T[][] arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(new Object[] {arg}) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01GenericArrayAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01GenericArrayAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(new Object[] {new Argument_GenericArray_Object()});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01GenericArrayAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01GenericArrayBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_GenericArray_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01GenericArrayBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01GenericArrayBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(Number[][] arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(new Object[] {arg}) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02GenericArrayBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01GenericArrayBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(Number[][] arg) {
        }


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02GenericArrayBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(Number[][] arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(new Object[] {arg}) );
        }
    }

    @ExprPointcut(Argument_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01GenericArrayBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01GenericArrayBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) Number[][] arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(new Object[] {arg}) );
        }
    }

    @ExprPointcut(Argument_GenericArray_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02GenericArrayAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02GenericArrayAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static Number[][] before(
                @Advice.Argument(0) Number[][] arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(new Object[] {arg}) );

            return arg;
        }
    }


    @Test
    public void testParameterizedCollection() {
        List<Integer> arg = new ArrayList<>();
        new Argument_ParameterizedCollection_Object<Integer>().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_ParameterizedCollection_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_ParameterizedCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01ParameterizedCollectionAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01ParameterizedCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01ParameterizedCollectionBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01ParameterizedCollectionBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02ParameterizedCollectionAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }
    }

    public static class Argument_ParameterizedCollection_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_ParameterizedCollection_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_ParameterizedCollection_Object.arguments(..))";


        public void arguments(List<Integer> arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01ParameterizedCollectionAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01ParameterizedCollectionAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(new Object[] {new ArrayList<>()});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01ParameterizedCollectionAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01ParameterizedCollectionBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_ParameterizedCollection_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01ParameterizedCollectionBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01ParameterizedCollectionBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(List<Integer> arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01ParameterizedCollectionBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(List<Integer> arg) {
        }


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02ParameterizedCollectionBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(List<Integer> arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01ParameterizedCollectionBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01ParameterizedCollectionBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) List<Integer> arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_ParameterizedCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02ParameterizedCollectionAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02ParameterizedCollectionAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static List<Integer> before(
                @Advice.Argument(0) List<Integer> arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }


    @Test
    public void testWildCardCollection() {
        List<Integer> arg = new ArrayList<>();
        new Argument_WildCardCollection_Object<Integer>().arguments(arg);

        Object[] origin = new Object[] {arg};

        Object[] modified = ExecutionMemento
                .getTargetMethodInvoker(Argument_WildCardCollection_Object.TARGET_METHOD)
                .getArguments();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(Argument_WildCardCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01WildCardCollectionAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_01Pojo_01WildCardCollectionAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_01WildCardCollectionBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_02AspectJ_02WildCardCollectionBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_01WildCardCollectionBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Arguments_ByteBuddy_02WildCardCollectionAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getArguments()).isEqualTo(origin);
        }
    }

    public static class Argument_WildCardCollection_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = Argument_WildCardCollection_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_03Arguments_Tests$Argument_WildCardCollection_Object.arguments(..))";


        public void arguments(List<? extends Number> arg) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(Argument_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_01Pojo_01WildCardCollectionAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = Arguments_01Pojo_01WildCardCollectionAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<Object[]> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set(joinpoint.getArguments());

            joinpoint.setArguments(new Object[] {new ArrayList<>()});

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );
        }


        private static final String AFTER_ADVICE = Arguments_01Pojo_01WildCardCollectionAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(joinpoint.getArguments()) );

            joinpoint.setArguments(ORIGIN.get());
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_01WildCardCollectionBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = Argument_WildCardCollection_Object.POINTCUT_EXPRESSION + " && args(arg)";


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_01WildCardCollectionBinding_Aspect.class.getName() + ".before";

        @Before(value = Arguments_02AspectJ_01WildCardCollectionBinding_Aspect.POINTCUT_EXPRESSION, argNames = "arg")
        public void before(List<? extends Number> arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @Aspect
    public static class Arguments_02AspectJ_02WildCardCollectionBinding_RP_Aspect {

        @Pointcut(Arguments_02AspectJ_01WildCardCollectionBinding_Aspect.POINTCUT_EXPRESSION)
        private void arguments(List<? extends Number> arg) {
        }


        private static final String BEFORE_ADVICE = Arguments_02AspectJ_02WildCardCollectionBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "arguments(arg)", argNames = "arg")
        public void before(List<? extends Number> arg) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_01WildCardCollectionBinding_Advisor {

        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_01WildCardCollectionBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.Argument(0) List<? extends Number> arg
                ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );
        }
    }

    @ExprPointcut(Argument_WildCardCollection_Object.POINTCUT_EXPRESSION)
    public static class Arguments_ByteBuddy_02WildCardCollectionAssigning_Advisor {
 
        private static final String BEFORE_ADVICE = Arguments_ByteBuddy_02WildCardCollectionAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToArguments( {
            @Advice.AssignReturned.ToArguments.ToArgument(value = 0, typing = Assigner.Typing.DYNAMIC)
        } )
        public static List<? extends Number> before(
                @Advice.Argument(0) List<? extends Number> arg
        ) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(arg) );

            return arg;
        }
    }
}
