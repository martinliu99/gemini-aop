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

/**
 * Tests target object binding and assigning of ByteBuddy advice via {@link net.bytebuddy.asm.Advice}
 * and {@link net.bytebuddy.asm.Advice.AssignReturned} annotations.
 *
 * @author   martin.liu
 */
public class Advice_07ByteBuddy_02TargetObject_Tests {

    @Test
    public void testTypeInitializer() {
        new TypeInitializer_Object();
        Object expected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(TypeInitializer_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(TypeInitializer_11IllegalAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class TypeInitializer_Object {

        private static final String TARGET_METHOD = TypeInitializer_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "staticinitialization(io.gemini.aop.integration.Advice_07ByteBuddy_02TargetObject_Tests$TypeInitializer_Object)";


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
                @Advice.This(optional = true) TypeInitializer_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(TypeInitializer_Object.POINTCUT_EXPRESSION)
    public static class TypeInitializer_11IllegalAssigning_Advisor {

        private static final String BEFORE_ADVICE = TypeInitializer_11IllegalAssigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThis
        public static TypeInitializer_Object before() {
            TypeInitializer_Object targetObject = new TypeInitializer_Object();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }


    @Test
    public void testClassMethod() {
        ClassMethod_Object.targetObject();
        Object expected = null;

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ClassMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
            assertThat(targetMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_01Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ClassMethod_11IllegalAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class ClassMethod_Object {

        private static final String TARGET_METHOD = ClassMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public static void io.gemini.aop.integration.Advice_07ByteBuddy_02TargetObject_Tests$ClassMethod_Object.targetObject())";


        public static void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(null) );
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_01Binding_Advisor {

        private static final String AFTER_ADVICE = ClassMethod_01Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void  after(
                @Advice.This(optional = true) ClassMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(ClassMethod_Object.POINTCUT_EXPRESSION)
    public static class ClassMethod_11IllegalAssigning_Advisor {

        private static final String BEFORE_ADVICE = ClassMethod_11IllegalAssigning_Advisor.class.getName() + ".before";

        @OnMethodExit(inline = false)
        @Advice.AssignReturned.ToThis
        public static ClassMethod_Object before() {
            ClassMethod_Object targetObject = new ClassMethod_Object();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }


    @Test
    public void testInstanceConstructor() {
        InstanceConstructor_Object object = new InstanceConstructor_Object(true);

        InstanceConstructor_Object expected = (InstanceConstructor_Object) ExecutionMemento
                .getAdviceMethodInvoker(InstanceConstructor_02Assigning_Advisor.AFTER_ADVICE)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceConstructor_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            Object captured = targetMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(object);
            assertThat(captured).isNotEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_01Binding_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_02Assigning_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isNotEqualTo(object);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_03Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            Object captured = afterAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(object);
            assertThat(captured).isNotEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceConstructor_11IllegalAssigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceConstructor_Object {

        private static final String TARGET_METHOD = InstanceConstructor_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public io.gemini.aop.integration.Advice_07ByteBuddy_02TargetObject_Tests$InstanceConstructor_Object.new(boolean))";


        public InstanceConstructor_Object(boolean targetObject) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }

        public InstanceConstructor_Object() {}
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_01Binding_Advisor {

        private static final String BEFORE_ADVICE = InstanceConstructor_01Binding_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        public static void after(
                @Advice.This(optional = true) InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_02Assigning_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_02Assigning_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        @Advice.AssignReturned.ToThis
        public static InstanceConstructor_Object after() {
            InstanceConstructor_Object targetObject = new InstanceConstructor_Object();

            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_03Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceConstructor_03Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.This(optional = true) InstanceConstructor_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }

    @ExprPointcut(InstanceConstructor_Object.POINTCUT_EXPRESSION)
    public static class InstanceConstructor_11IllegalAssigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceConstructor_11IllegalAssigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThis
        public static InstanceConstructor_Object before() {
            InstanceConstructor_Object targetObject = new InstanceConstructor_Object();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }


    @Test
    public void testInstanceMethod() {
        InstanceMethod_Object object = new InstanceMethod_Object();
        object.targetObject();

        InstanceMethod_Object expected = (InstanceMethod_Object) ExecutionMemento
                .getAdviceMethodInvoker(InstanceMethod_01Assigning_Advisor.BEFORE_ADVICE)
                .getTargetObject();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(InstanceMethod_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();

            Object captured = targetMethodInvoker.getTargetObject();
            assertThat(captured).isNotEqualTo(object);
            assertThat(captured).isEqualTo(expected);
        }

        {
            AdviceMethod beforeAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_01Assigning_Advisor.BEFORE_ADVICE);
            assertThat(beforeAdviceMethodInvoker).isNotNull();
            assertThat(beforeAdviceMethodInvoker.getTargetObject()).isEqualTo(expected);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_02Binding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();

            Object captured = afterAdviceMethodInvoker.getTargetObject();
            assertThat(captured).isEqualTo(expected);
            assertThat(captured).isNotEqualTo(object);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(InstanceMethod_11IllegalBinding_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    public static class InstanceMethod_Object {

        private static final String TARGET_METHOD = InstanceMethod_Object.class.getName() + ".targetObject";

        private static final String POINTCUT_EXPRESSION = 
                "execution(public void io.gemini.aop.integration.Advice_07ByteBuddy_02TargetObject_Tests$InstanceMethod_Object.targetObject())";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_01Assigning_Advisor {

        private static final String BEFORE_ADVICE = InstanceMethod_01Assigning_Advisor.class.getName() + ".before";

        @OnMethodEnter(inline = false)
        @Advice.AssignReturned.ToThis
        public static InstanceMethod_Object before() {
            InstanceMethod_Object targetObject = new InstanceMethod_Object();

            ExecutionMemento.putAdviceMethodInvoker(BEFORE_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );

            return targetObject;
        }
    }

    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_02Binding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_02Binding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false, backupArguments = false)
        public static void after(
                @Advice.This(optional = true) InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }


    @ExprPointcut(InstanceMethod_Object.POINTCUT_EXPRESSION)
    public static class InstanceMethod_11IllegalBinding_Advisor {

        private static final String AFTER_ADVICE = InstanceMethod_11IllegalBinding_Advisor.class.getName() + ".after";

        @OnMethodExit(inline = false)
        public static void after(
                @Advice.This(readOnly = false) InstanceMethod_Object targetObject) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withTargetObject(targetObject) );
        }
    }
}
