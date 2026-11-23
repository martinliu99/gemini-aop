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

import java.lang.reflect.AccessibleObject;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;

/**
 * Tests AspectJ pointcut expression matching.
 *
 * @author   martin.liu
 */
public class Pointcut_06AspectJExpr_Tests {

    @Test
    public void testLogicalOperator() {
        LogicalOperator_Object object = new LogicalOperator_Object();
        object.matchLogicalOperator(1l);

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(LogicalOperator_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        ExecutionMemento.clearMemento();


        object.matchLogicalOperator( Long.valueOf(1l) );

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(LogicalOperator_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class LogicalOperator_Object {

        public long matchLogicalOperator(long input) {
            return input;
        }

        public Long matchLogicalOperator(Long input) {
            return input;
        }
    }

    @Aspect
    public static class LogicalOperator_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$LogicalOperator_Object.matchLogicalOperator(long))"
                + " or "
                + "execution(!private java.lang.Long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$LogicalOperator_Object.matchLogicalOperator(java.lang.Long))";

        private static final String AFTER_ADVICE = LogicalOperator_Aspect.class.getName() + ".after";


        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testReferencePointcut() {
        NamedPointcut_Object object = new NamedPointcut_Object();
        object.match1(1l);

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(LocalRefPointcut_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(CrossClassRefPointcut_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        ExecutionMemento.clearMemento();


        object.match2( Long.valueOf(1l) );

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(LocalRefPointcut_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(CrossClassRefPointcut_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class NamedPointcut_Object {

        public long match1(long input) {
            return input;
        }

        public Long match2(Long input) {
            return input;
        }
    }

    @Aspect
    public static class LocalRefPointcut_Aspect {

        private static final String AFTER_ADVICE = LocalRefPointcut_Aspect.class.getName() + ".after";

        @Pointcut("execution(!private long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$NamedPointcut_Object.match1(long))")
        public void match1() { }

        @Pointcut("execution(!private java.lang.Long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$NamedPointcut_Object.match2(java.lang.Long))")
        public void match2() { }

        @After("match1() or match2()")
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class CrossClassRefPointcut_Aspect {

        private static final String AFTER_ADVICE = CrossClassRefPointcut_Aspect.class.getName() + ".after";

        @Pointcut("execution(!private long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$NamedPointcut_Object.match1(long))")
        public void match1() { }

        @Pointcut("execution(!private java.lang.Long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$NamedPointcut_Object.match2(java.lang.Long))")
        public void match2() { }

        @After("io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$LocalRefPointcut_Aspect.match1() or io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$LocalRefPointcut_Aspect.match2()")
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testPlaceholder() {
        Placeholder_Object object = new Placeholder_Object();
        object.matchPlaceholder(1l);

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Placeholder_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Placeholder_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        ExecutionMemento.clearMemento();


        object.matchPlaceholder( Long.valueOf(1l) );

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Placeholder_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(Placeholder_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class Placeholder_Object {

        public long matchPlaceholder(long input) {
            return input;
        }

        public Long matchPlaceholder(Long input) {
            return input;
        }
    }

    @Aspect
    public static class Placeholder_Aspect {

        private static final String AFTER_ADVICE = Placeholder_Aspect.class.getName() + ".after";

        @After("${user.Pointcut_01JoinpointMatching_Tests.placeholderAdvisorsPointcutExpr}")
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class Placeholder_RP_Aspect {

        private static final String AFTER_ADVICE = Placeholder_RP_Aspect.class.getName() + ".after";

        @Pointcut("${user.Pointcut_01JoinpointMatching_Tests.placeholderAdvisorsPointcutExpr2}")
        public void matchPlaceholder2() { }

        @Pointcut("${user.Pointcut_01JoinpointMatching_Tests.placeholderAdvisorsPointcutExpr3}")
        public void matchPlaceholder3() { }

        @After("matchPlaceholder2() or matchPlaceholder3()")
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testArgsBinding() {
        ArgBinding_Object object = new ArgBinding_Object();
        long arg = 1L;
        long arg2 = 2L;
        long returnValue = object.match(arg, arg2);
        Object[] arguments = new Object[] {arg, arg2};

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ArgBinding_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(object);
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(arguments);
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(returnValue);
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ArgBinding_RP_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
            assertThat(afterAdviceMethodInvoker.getTargetObject()).isEqualTo(object);
            assertThat(afterAdviceMethodInvoker.getArguments()).isEqualTo(arguments);
            assertThat(afterAdviceMethodInvoker.getReturnValue()).isEqualTo(returnValue);
        }
    }

    public static class ArgBinding_Object {

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private long io.gemini.aop.integration.Pointcut_06AspectJExpr_Tests$ArgBinding_Object.match(..)) && args(arg, arg2) && target(targetObject)";

        public long match(long arg, long arg2) {
            return arg + arg2;
        }

    }

    @Aspect
    public static class ArgBinding_Aspect {

        private static final String AFTER_ADVICE = Placeholder_Aspect.class.getName() + ".after";

        @AfterReturning(value = ArgBinding_Object.POINTCUT_EXPRESSION, returning = "returnValue")
        public void after(AccessibleObject staticPart, long returnValue, ArgBinding_Object targetObject, long arg, long arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withStaticPart(staticPart)
                    .withTargetObject(targetObject)
                    .withArgumnts(arg, arg2) 
                    .withReturnValue(returnValue)
            );
        }
    }

    @Aspect
    public static class ArgBinding_RP_Aspect {

        @Pointcut(ArgBinding_Object.POINTCUT_EXPRESSION)
        public void match(long returnValue, ArgBinding_Object targetObject, long arg, long arg2) { }

        private static final String AFTER_ADVICE = Placeholder_Aspect.class.getName() + ".after";

        @AfterReturning(value = "match(returnValue, targetObject, arg, arg2)", returning = "returnValue")
        public void after(AccessibleObject staticPart, long returnValue, ArgBinding_Object targetObject, long arg, long arg2) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                    .withStaticPart(staticPart)
                    .withTargetObject(targetObject)
                    .withArgumnts(arg, arg2)
                    .withReturnValue(returnValue)
            );
        }
    }
}