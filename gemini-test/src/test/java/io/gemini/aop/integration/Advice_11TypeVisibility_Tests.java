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

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;

import io.gemini.aop.integration.Advice_11TargetVisibility_Object.ArgumentsVisibility_Object;
import io.gemini.aop.integration.Advice_11TargetVisibility_Object.ParameterizedExceptionTypeVisibility_Object;
import io.gemini.aop.integration.Advice_11TargetVisibility_Object.ParameterizedReturnValueVisibility_Object;
import io.gemini.aop.integration.Advice_11TargetVisibility_Object.ReturnValueVisibility_Object;
import io.gemini.aop.integration.Advice_11TargetVisibility_Object.TargetObjectVisibility_Object;
import io.gemini.aop.integration.Advice_11TargetVisibility_Object.ThrownExceptionVisibility_Object;
import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodExit;

/**
 * Tests visibility of target type referred by advice method.
 *
 * @author   martin.liu
 */
public class Advice_11TypeVisibility_Tests {

    @Test
    public void testTargetObjectVisibility() {
        Object object = new Advice_11TargetVisibility_Object().testTargetObjectVisibility();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObjectVisibility_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(object);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObjectVisibility_ByteBuddy_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(object);
        }
    }

    @Aspect
    public static class TargetObjectVisibility_Aspect {

        public static final String POINTCUT_EXPRESSION = 
                "execution(!private long io.gemini.aop.integration.Advice_11TargetVisibility_Object$TargetObjectVisibility_Object.targetObject())";


        private static final String AFTER_ADVICE = TargetObjectVisibility_Aspect.class.getName() + ".targetObject_after";

        @After(value = POINTCUT_EXPRESSION + " && this(targetObject)", argNames = "targetObject")
        public static void targetObject_after(MutableJoinpoint<Long, RuntimeException> joinpoint, TargetObjectVisibility_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObjectVisibility_Aspect.POINTCUT_EXPRESSION)
    public static class TargetObjectVisibility_ByteBuddy_Advisor {

        private static final String AFTER_ADVICE = TargetObjectVisibility_ByteBuddy_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.This TargetObjectVisibility_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @Test
    public void testArgumentsVisibility() {
        ArgumentsVisibility_Object object = new ArgumentsVisibility_Object();
        ArgumentsVisibility_Object.Argument_Object arg = new ArgumentsVisibility_Object.Argument_Object();
        object.arguments(arg);

        Object[] expectedArgs = new Object[] {arg};

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ArgumentsVisibility_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo( expectedArgs );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ArgumentsVisibility_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo( expectedArgs );
        }
    }

    @Aspect
    public static class ArgumentsVisibility_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private * io.gemini.aop.integration.Advice_11TargetVisibility_Object$ArgumentsVisibility_Object.arguments(..))";


        private static final String AFTER_ADVICE = ArgumentsVisibility_Aspect.class.getName() + ".arguments_after";

        @After(value = POINTCUT_EXPRESSION + " && args(argument)", argNames = "argument")
        public void arguments_after(
                MutableJoinpoint<ArgumentsVisibility_Object.Argument_Object, RuntimeException> joinpoint, 
                ArgumentsVisibility_Object.Argument_Object argument) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(argument) );
        }
    }

    @ExprPointcut(ArgumentsVisibility_Aspect.POINTCUT_EXPRESSION)
    public static class ArgumentsVisibility_Advisor {

        private static final String AFTER_ADVICE = ArgumentsVisibility_Advisor.class.getName() + ".after";


        @OnMethodExit(inline = false)
        public static void after(
                @Advice.AllArguments Object[] args) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withArgumnts(args) );
        }
    }


    @Test
    public void testReturnValueVisibility() {
        ReturnValueVisibility_Object object = new ReturnValueVisibility_Object();
        Object expectedReturnValue = object.returnValue();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValueVisibility_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo( expectedReturnValue );
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValueVisibility_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo( expectedReturnValue );
        }
    }

    @Aspect
    public static class ReturnValueVisibility_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(* io.gemini.aop.integration.Advice_11TargetVisibility_Object$ReturnValueVisibility_Object.returnValue())";


        private static final String AFTER_ADVICE = ReturnValueVisibility_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterReturning(value = POINTCUT_EXPRESSION, returning = "returnValue")
        public void after(MutableJoinpoint joinpoint, ReturnValueVisibility_Object.ReturnValue_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }

    @ExprPointcut(ReturnValueVisibility_Aspect.POINTCUT_EXPRESSION)
    public static class ReturnValueVisibility_Advisor {

        private static final String AFTER_ADVICE = ReturnValueVisibility_Advisor.class.getName() + ".returnValue_after";

        @OnMethodExit(inline = false)
        public static void returnValue_after(
                @Advice.Return ReturnValueVisibility_Object.ReturnValue_Object returnValue) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(returnValue) );
        }
    }


    @Test
    public void testThrownExceptionVisibility() {
        try {
            new ThrownExceptionVisibility_Object( new ThrownExceptionVisibility_Object.ExceptionA_Object("expected") )
            .thrownException();
            assertThat(false).isTrue();
        } catch (Exception actualException) {
            {
                AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownExceptionVisibility_Aspect.AFTER_ADVICE);
                assertThat(afterAdviceMethodInvoker).isNotNull();
                assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo( actualException );
            }

            {
                AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ThrownExceptionVisibility_Advisor.AFTER_ADVICE);
                assertThat(afterAdviceMethodInvoker).isNotNull();
                assertThat(afterAdviceMethodInvoker.getThrownException()).isEqualTo( actualException );
            }
        }
    }

    @Aspect
    public static class ThrownExceptionVisibility_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private void io.gemini.aop.integration.Advice_11TargetVisibility_Object$ThrownExceptionVisibility_Object.thrownException())";


        private static final String AFTER_ADVICE = ThrownExceptionVisibility_Aspect.class.getName() + ".after";

        @SuppressWarnings("rawtypes")
        @AfterThrowing(value = POINTCUT_EXPRESSION, throwing = "thrownException")
        public void after(MutableJoinpoint joinpoint, ThrownExceptionVisibility_Object.ExceptionA_Object thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }

    @ExprPointcut(ThrownExceptionVisibility_Aspect.POINTCUT_EXPRESSION)
    public static class ThrownExceptionVisibility_Advisor {

        private static final String AFTER_ADVICE = ThrownExceptionVisibility_Advisor.class.getName() + ".after";


        @OnMethodExit(inline = false, onThrowable = ThrownExceptionVisibility_Object.ExceptionA_Object.class)
        public static void after(
                @Advice.Thrown ThrownExceptionVisibility_Object.ExceptionA_Object thrownException) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(thrownException) );
        }
    }


    @Test
    public void testParameterizedReturnValueVisibility() {
        ParameterizedReturnValueVisibility_Object object = new ParameterizedReturnValueVisibility_Object();
        object.parameterizedReturnValue();

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ParameterizedReturnTypeVisibility_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    @Aspect
    public static class ParameterizedReturnTypeVisibility_Aspect {

        private static final String AFTER_ADVICE = ParameterizedReturnTypeVisibility_Aspect.class.getName() + ".after";

        @After(value = "execution(!private io.gemini.aop.integration.Advice_11TargetVisibility_Object$ParameterizedReturnValueVisibility_Object$ReturnValue_Object io.gemini.aop.integration.Advice_11TargetVisibility_Object$ParameterizedReturnValueVisibility_Object.parameterizedReturnValue())")
        public void after(MutableJoinpoint<ParameterizedReturnValueVisibility_Object.ReturnValue_Object, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testParameterizedExceptionTypeVisibility() {
        try {
            new ParameterizedExceptionTypeVisibility_Object( new ParameterizedExceptionTypeVisibility_Object.ExceptionA_Object("expected") )
            .parameterizedExceptionType();

            assertThat(false).isTrue();
        } catch (Exception actualException) {
            {
                AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(parameterizedExceptionType_Aspect.AFTER_ADVICE);
                assertThat(afterAdviceMethodInvoker).isNull();
            }
        }
    }

    @Aspect
    public static class parameterizedExceptionType_Aspect {

        private static final String AFTER_ADVICE = parameterizedExceptionType_Aspect.class.getName() + ".after";

        @After(value = "execution(!private void io.gemini.aop.integration.Advice_11TargetVisibility_Object$ParameterizedExceptionTypeVisibility_Object.parameterizedExceptionType())")
        public void after(MutableJoinpoint<Void, ParameterizedExceptionTypeVisibility_Object.ExceptionA_Object> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withThrownException(joinpoint.getThrownException()) );
        }
    }
}
