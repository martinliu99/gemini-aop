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

import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.AdviceMethod;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.Pointcut;
import io.gemini.api.aop.annotation.PojoPointcut;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Tests type parameters matching of {@code MutableJoinpoint} argument.
 *
 * @author   martin.liu
 */
public class MutableJoinpoint_21ParameterizedReturnType_Tests {

    @Test
    public void testVoidType() {
        new ReturnValue_VoidType_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_VoidType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01VoidTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_11IllegalVoidTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01VoidTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalVoidTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class ReturnValue_VoidType_Object {

        public static final String TARGET_METHOD = ReturnValue_VoidType_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(* io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_VoidType_Object.returnValue())";


        private void returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01VoidTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_01VoidTypeMatching_Advisor extends Advice.AbstractAfter<Void, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01VoidTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Void, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_VoidType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPrivate());
        }
    }


    @PojoPointcut(ReturnValue_Pojo_01VoidTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_11IllegalVoidTypeMatching_Advisor extends Advice.AbstractAfter<Object, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_11IllegalVoidTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Object, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_VoidType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPrivate());
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01VoidTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01VoidTypeMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Void, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalVoidTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalVoidTypeMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_VoidType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Object, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testPrimitiveType() {
        ReturnValue_PrimitiveType_Object object = new ReturnValue_PrimitiveType_Object();
        object.returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_PrimitiveType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01PrimitiveTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_11IllegalPrimitiveTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01PrimitiveTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalPrimitiveTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class ReturnValue_PrimitiveType_Object {

        public static final String TARGET_METHOD = ReturnValue_PrimitiveType_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(!private * io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_PrimitiveType_Object.returnValue())";


        public int returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return 1;
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01PrimitiveTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_01PrimitiveTypeMatching_Advisor extends Advice.AbstractAfter<Integer, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01PrimitiveTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Integer, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_PrimitiveType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(int.class));
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01PrimitiveTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_11IllegalPrimitiveTypeMatching_Advisor extends Advice.AbstractAfter<Number, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_11IllegalPrimitiveTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Number, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_PrimitiveType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(int.class));
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01PrimitiveTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01PrimitiveTypeMatching_Aspect.class.getName() + ".after";


        @After(ReturnValue_PrimitiveType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Integer, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalPrimitiveTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalPrimitiveTypeMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_PrimitiveType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Long, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericType() {
        new ReturnValue_GenericType_Object<Long>().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericType_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01GenericTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_11llegalGenericTypeMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01GenericTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalGenericTypeMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class ReturnValue_GenericType_Object<T extends Number & Comparable<T>> {

        public static final String TARGET_METHOD = ReturnValue_GenericType_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.lang.Number io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericType_Object"
                + ".returnValue())";


        public T returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01GenericTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_01GenericTypeMatching_Advisor extends Advice.AbstractAfter<Number, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01GenericTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Number, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(Number.class));
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01GenericTypeMatching_Advisor.class)
    public static class ReturnValue_Pojo_11llegalGenericTypeMatching_Advisor extends Advice.AbstractAfter<Integer, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_11llegalGenericTypeMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Integer, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericType_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(Number.class));
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01GenericTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01GenericTypeMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Number, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalGenericTypeMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalGenericTypeMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_GenericType_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Integer, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testGenericArray() {
        new ReturnValue_GenericArray_Object<Long>().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_GenericArray_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01GenericArrayMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_11IllegalGenericArrayMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01GenericArrayMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_11IllegalGenericArrayMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNull();
        }
    }

    static class ReturnValue_GenericArray_Object<T extends Number & Comparable<T>> {

        public static final String TARGET_METHOD = ReturnValue_GenericArray_Object.class.getName() + ".returnValue";

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.lang.Number[][] io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericArray_Object.returnValue())";


        public T[][] returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01GenericArrayMatching_Advisor.class)
    public static class ReturnValue_Pojo_01GenericArrayMatching_Advisor extends Advice.AbstractAfter<Number[][], RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01GenericArrayMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Number[][], RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericArray_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(Number[][].class));
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01GenericArrayMatching_Advisor.class)
    public static class ReturnValue_Pojo_11IllegalGenericArrayMatching_Advisor extends Advice.AbstractAfter<Integer[][], RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_11IllegalGenericArrayMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Integer[][], RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_GenericArray_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(Number[].class));
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01GenericArrayMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01GenericArrayMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Number[][], RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_11IllegalGenericArrayMatching_Aspect {

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_11IllegalGenericArrayMatching_Aspect.class.getName() + ".after";

        @After(ReturnValue_GenericArray_Object.POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<Integer[][], RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod() );
        }
    }


    @Test
    public void testParameterizedCollection() {
        new ReturnValue_ParameterizedCollection_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_ParameterizedCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_Pojo_01ParameterizedCollectionMatching_Advisor.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class ReturnValue_ParameterizedCollection_Object {

        public static final String TARGET_METHOD = ReturnValue_ParameterizedCollection_Object.class.getName() + ".returnValue";

        public List<Integer> returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @PojoPointcut(ReturnValue_Pojo_01ParameterizedCollectionMatching_Advisor.class)
    public static class ReturnValue_Pojo_01ParameterizedCollectionMatching_Advisor extends Advice.AbstractAfter<List<Integer>, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_Pojo_01ParameterizedCollectionMatching_Advisor.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<List<Integer>, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_ParameterizedCollection_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(List.class));
        }
    }

    @Aspect
    public static class ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<java.lang.Integer> io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_ParameterizedCollection_Object"
                + ".returnValue())";

        private static final String AFTER_ADVICE = ReturnValue_AspectJ_01ParameterizedCollectionMatching_Aspect.class.getName() + ".after";

        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<List<Integer>, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }


    @Test
    public void testWildCardCollection() {
        new ReturnValue_WildCardCollection_Object().returnValue();

        {
            TargetMethod targetMethodInvoker = ExecutionMemento.getTargetMethodInvoker(ReturnValue_WildCardCollection_Object.TARGET_METHOD);
            assertThat(targetMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_WildCardCollectionMatching_Advice.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }

        {
            AdviceMethod afterAdviceMethodInvoker = ExecutionMemento.getAdviceMethodInvoker(ReturnValue_WildCardCollectionMatching_Aspect.AFTER_ADVICE);
            assertThat(afterAdviceMethodInvoker).isNotNull();
        }
    }

    static class ReturnValue_WildCardCollection_Object {

        public static final String TARGET_METHOD = ReturnValue_WildCardCollection_Object.class.getName() + ".returnValue";

        public List<? extends Number> returnValue() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod() );

            return null;
        }
    }

    @PojoPointcut(ReturnValue_WildCardCollectionMatching_Advice.class)
    public static class ReturnValue_WildCardCollectionMatching_Advice extends Advice.AbstractAfter<List<? extends Number>, RuntimeException> implements Pointcut {

        private static final String AFTER_ADVICE = ReturnValue_WildCardCollectionMatching_Advice.class.getName() + ".after";

        /**
         * {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<List<? extends Number>, RuntimeException> joinpoint) throws Throwable {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return named("io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_WildCardCollection_Object");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return named("returnValue")
                    .and(isPublic())
                    .and(returns(List.class));
        }
    }

    @Aspect
    public static class ReturnValue_WildCardCollectionMatching_Aspect {

        private static final String POINTCUT_EXPRESSION = 
                "execution(java.util.List<? extends java.lang.Number> io.gemini.aop.integration.MutableJoinpoint_21ParameterizedReturnType_Tests$ReturnValue_WildCardCollection_Object"
                + ".returnValue(..))";

        private static final String AFTER_ADVICE = ReturnValue_WildCardCollectionMatching_Aspect.class.getName() + ".after";

        @After(POINTCUT_EXPRESSION)
        public void after(MutableJoinpoint<List<? extends Number>, RuntimeException> joinpoint) {
            ExecutionMemento.putAdviceMethodInvoker(AFTER_ADVICE, 
                    new AdviceMethod()
                        .withReturnValue(joinpoint.getReturnValue()) );
        }
    }
}