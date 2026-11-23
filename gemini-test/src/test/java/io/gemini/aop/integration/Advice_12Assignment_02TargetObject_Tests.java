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

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ExprPointcut;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;

/**
 * Tests assignment of target type referred by advice method.
 * 
 * @author   martin.liu
 */
public class Advice_12Assignment_02TargetObject_Tests {

    @Test
    public void testUpcastedType() {
        TargetObject_Super_Object origin = new TargetObject_Super_Object();
        origin.targetObject();

        TargetObject_Super_Object modified = (TargetObject_Super_Object) ExecutionMemento
                .getTargetMethodInvoker(TargetObject_Super_Object.TARGET_METHOD)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TargetObject_Super_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_01UpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_01UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_02UpcastedTypeBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_03ByteBuddy_01UpcastedTypeBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_03ByteBuddy_02UpcastedTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_03ByteBuddy_03UpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isInstanceOf(TargetObject_Sub_Object.class);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_03ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_03ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static abstract class TargetObject_Base_Object {
        
    }

    public static class TargetObject_Super_Object extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = TargetObject_Super_Object.class.getName() + ".targetObject";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_02TargetObject_Tests$TargetObject_Super_Object.targetObject(..))";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    public static class TargetObject_Sub_Object extends TargetObject_Super_Object {
        
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_01Pojo_01UpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = TargetObject_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<TargetObject_Super_Object> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set( (TargetObject_Super_Object) joinpoint.getTargetObject());

            TargetObject_Super_Object targetObject = new TargetObject_Super_Object();
            joinpoint.setTargetObject(targetObject);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }


        private static final String AFTER_ADVICE = TargetObject_01Pojo_01UpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );

            joinpoint.setTargetObject(ORIGIN.get());
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_01Pojo_11IllegalUpcastedTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = TargetObject_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".before";


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setTargetObject(1);

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }


        private static final String AFTER_ADVICE = TargetObject_01Pojo_11IllegalUpcastedTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            joinpoint.setTargetObject(1);

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = TargetObject_Super_Object.POINTCUT_EXPRESSION + " && target(targetObject)";


        private static final String BEFORE_ADVICE = TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect.class.getName() + ".before";

        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before(TargetObject_Super_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_02UpcastedTypeBinding_RP_Aspect {

        @Pointcut(TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION)
        public void targetObject(TargetObject_Base_Object targetObject) {  }

        private static final String BEFORE_ADVICE = TargetObject_02AspectJ_02UpcastedTypeBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        public void before(TargetObject_Base_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_11IllegalUpcastedTypeBinding_Aspect {

        private static final String AFTER_ADVICE = TargetObject_02AspectJ_11IllegalUpcastedTypeBinding_Aspect.class.getName() + ".after";

        @After(value = TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void after(Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect {

        @Pointcut(TargetObject_02AspectJ_01UpcastedTypeBinding_Aspect.POINTCUT_EXPRESSION)
        public void targetObject(Long targetObject) {  }

        private static final String AFTER_ADVICE = TargetObject_02AspectJ_12IllegalUpcastedTypeBinding_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        public void after(Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_03ByteBuddy_01UpcastedTypeBinding_Advisor {

        private static final String BEFORE_ADVICE = TargetObject_03ByteBuddy_01UpcastedTypeBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.This(optional = true) TargetObject_Base_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_03ByteBuddy_02UpcastedTypeAssigning_Advisor {

        private static final String BEFORE_ADVICE = TargetObject_03ByteBuddy_02UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThis
        public static TargetObject_Super_Object before(
                @Advice.This(optional = true) TargetObject_Super_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_03ByteBuddy_03UpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = TargetObject_03ByteBuddy_03UpcastedTypeAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToThis
        public static TargetObject_Sub_Object before() {
            TargetObject_Sub_Object targetObject = new TargetObject_Sub_Object();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_03ByteBuddy_11IllegalUpcastedTypeBinding_Advisor {

        private static final String AFTER_ADVICE = TargetObject_03ByteBuddy_11IllegalUpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.This(optional = true) Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_Super_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_03ByteBuddy_12IllegalUpcastedTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = TargetObject_03ByteBuddy_01UpcastedTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToThis
        public static Long after() {
            Long targetObject = 1L;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGenericType() {
        TargetObject_GenericType_Object<Integer> origin = new TargetObject_GenericType_Object<Integer>();
        origin.targetObject();

        TargetObject_GenericType_Object modified = (TargetObject_GenericType_Object) ExecutionMemento
                .getTargetMethodInvoker(TargetObject_GenericType_Object.TARGET_METHOD)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TargetObject_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_01GenericTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);

            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_01Pojo_01GenericTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_01GenericTypeBinding_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_02GenericTypeBinding_RP_Aspect.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(modified);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_11IllegalGenericTypeBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_02AspectJ_12IllegalGenericTypeBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_ByteBuddy_01GenericTypeBinding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(origin);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_ByteBuddy_02GenericTypeAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(origin);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_ByteBuddy_11IllegalGenericTypeBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TargetObject_ByteBuddy_12IllegalGenericTypeAssigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class TargetObject_GenericType_Object<T extends Number & Comparable<T>> extends TargetObject_Base_Object {

        public static final String TARGET_METHOD = TargetObject_GenericType_Object.class.getName() + ".targetObject";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Advice_12Assignment_02TargetObject_Tests$TargetObject_GenericType_Object.targetObject(..))";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @SuppressWarnings("rawtypes")
    @ExprPointcut(TargetObject_GenericType_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_01Pojo_01GenericTypeAssigning_Advisor extends io.gemini.api.aop.Advice.AbstractBeforeAfter {

        private static final String BEFORE_ADVICE = TargetObject_01Pojo_01GenericTypeAssigning_Advisor.class.getName() + ".before";

        private static final ThreadLocal<TargetObject_GenericType_Object> ORIGIN = new ThreadLocal<>();


        /** 
         * {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint joinpoint) throws Throwable {
            ORIGIN.set( (TargetObject_GenericType_Object) joinpoint.getTargetObject());

            joinpoint.setTargetObject(new TargetObject_GenericType_Object<>());

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );
        }


        private static final String AFTER_ADVICE = TargetObject_01Pojo_01GenericTypeAssigning_Advisor.class.getName() + ".after";

        /** 
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(joinpoint.getTargetObject()) );

            joinpoint.setTargetObject(ORIGIN.get());
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_01GenericTypeBinding_Aspect {

        private static final String POINTCUT_EXPRESSION = TargetObject_GenericType_Object.POINTCUT_EXPRESSION + " && target(targetObject)";


        private static final String BEFORE_ADVICE = TargetObject_02AspectJ_01GenericTypeBinding_Aspect.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @Before(value = POINTCUT_EXPRESSION, argNames = "targetObject")
        public void before(TargetObject_GenericType_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_02GenericTypeBinding_RP_Aspect {

        @Pointcut(TargetObject_02AspectJ_01GenericTypeBinding_Aspect.POINTCUT_EXPRESSION)
        public void targetObject(TargetObject_Base_Object targetObject) {  }

        private static final String BEFORE_ADVICE = TargetObject_02AspectJ_02GenericTypeBinding_RP_Aspect.class.getName() + ".before";

        @Before(value = "targetObject(targetObject)", argNames = "targetObject")
        public void before(TargetObject_Base_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_11IllegalGenericTypeBinding_Aspect {

        private static final String AFTER_ADVICE = TargetObject_02AspectJ_11IllegalGenericTypeBinding_Aspect.class.getName() + ".after";

        @After(value = TargetObject_02AspectJ_01GenericTypeBinding_Aspect.POINTCUT_EXPRESSION, argNames = "targetObject")
        public void after(Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @Aspect
    public static class TargetObject_02AspectJ_12IllegalGenericTypeBinding_RP_Aspect {

        @Pointcut(TargetObject_02AspectJ_01GenericTypeBinding_Aspect.POINTCUT_EXPRESSION)
        public void targetObject(Long targetObject) {  }

        private static final String AFTER_ADVICE = TargetObject_02AspectJ_12IllegalGenericTypeBinding_RP_Aspect.class.getName() + ".after";

        @After(value = "targetObject(targetObject)", argNames = "targetObject")
        public void after(Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_GenericType_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_ByteBuddy_01GenericTypeBinding_Advisor {

        private static final String BEFORE_ADVICE = TargetObject_ByteBuddy_01GenericTypeBinding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void before(
                @Advice.This(optional = true) TargetObject_Base_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_GenericType_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_ByteBuddy_02GenericTypeAssigning_Advisor {

        private static final String BEFORE_ADVICE = TargetObject_ByteBuddy_02GenericTypeAssigning_Advisor.class.getName() + ".before";

        @SuppressWarnings("rawtypes")
        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThis
        public static TargetObject_GenericType_Object before(
                @Advice.This(optional = true) TargetObject_GenericType_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @ExprPointcut(TargetObject_GenericType_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_ByteBuddy_11IllegalGenericTypeBinding_Advisor {

        private static final String AFTER_ADVICE = TargetObject_ByteBuddy_11IllegalGenericTypeBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.This(optional = true) Long targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TargetObject_GenericType_Object.POINTCUT_EXPRESSION)
    public static class TargetObject_ByteBuddy_12IllegalGenericTypeAssigning_Advisor {

        private static final String AFTER_ADVICE = TargetObject_ByteBuddy_12IllegalGenericTypeAssigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToThis
        public static Long after() {
            Long targetObject = 1L;

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }
}
