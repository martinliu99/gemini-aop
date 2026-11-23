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
package io.gemini.aop.factory.support;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import io.gemini.aop.factory.support.AdviceSpec.AspectJAdviceSpec;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.core.util.ClassUtils;
import io.gemini.core.util.MethodHandleLookup;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Abstract base class of AspectJ advice adapters.
 * Holds the AspectJ aspect instance and provides argument extraction from the joinpoint.
 */
public abstract class AspectJAdviceAdapter<A> extends Advice.AbstractBase {

    static final TypeDescription ASPECTJ_ADPATER_TYPE = TypeDescription.ForLoadedType.of(AspectJAdviceAdapter.class);

    static final MethodDescription CONSTRUCTOR;
    static final String CONSTRUCTOR_PARAM0_ADVICE_SPEC = "adviceSpec";
    static final String CONSTRUCTOR_PARAM1_DELEGATE_OBJECT = "delegateObject";

    static final MethodDescription GET_DELEGATE_OBJECT_METHOD;


    private final AspectJAdviceSpec.Loaded adviceSpec;
    private final A delegateObject;


    static {
        MethodDescription constructor = null;
        try {
            constructor = ASPECTJ_ADPATER_TYPE.getDeclaredMethods().filter(
                    isConstructor()
                    .and( takesArguments(AspectJAdviceSpec.Loaded.class, Object.class) )
            )
            .getOnly();
        } catch (Exception e) { /* do nothing*/ }
        CONSTRUCTOR = constructor;

        MethodDescription method = null;
        try {
            method = ASPECTJ_ADPATER_TYPE.getDeclaredMethods().filter(
                    named("getDelegateObject")
                    .and( returns( Object.class) )
            )
            .getOnly();
        } catch (Exception e) { /* do nothing*/ }
        GET_DELEGATE_OBJECT_METHOD = method;
    }


    public AspectJAdviceAdapter(AspectJAdviceSpec.Loaded adviceSpec, A delegateObject) 
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        this.adviceSpec = adviceSpec;
        this.delegateObject = delegateObject;
    }

    protected A getDelegateObject() {
        return delegateObject;
    }

    protected MethodHandle getAdviceMethod(AspectJAdviceSpec.Loaded adviceSpec, Object delegateObject) 
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        Class<?> delegateClass = delegateObject.getClass();
        ClassLoader classLoader = delegateClass.getClassLoader();

        MethodDescription adviceMethod = adviceSpec.getAdviceMethod();
        Class<?> returnType = ClassUtils.forName(adviceMethod.getReturnType().getTypeName(), classLoader);

        List<ResolvedParameter> resolvedParameters = adviceSpec.getResolvedParameters();
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>(resolvedParameters.size());
        for (ResolvedParameter resolvedParameter : resolvedParameters) {
            parameterTypes.add( ClassUtils.forName(resolvedParameter.getMatchedType().getTypeName(), classLoader) );
        }

        MethodType methodType = MethodType.methodType(returnType, parameterTypes);

        MethodHandleLookup lookup = MethodHandleLookup.select(delegateClass, MethodHandles.lookup());
        if (adviceMethod.isStatic())
            return lookup.findStatic(delegateClass, adviceMethod.getName(), methodType);
        else 
            return lookup.findVirtual(delegateClass, adviceMethod.getName(), methodType).bindTo(delegateObject);
    }


    protected Object[] getArguments(MutableJoinpoint<Object, Throwable> joinpoint) {
        int index = 0;
        List<ResolvedParameter> resolvedParameters = adviceSpec.getResolvedParameters();
        Object[] args = new Object[resolvedParameters.size()];
        for (ResolvedParameter resolvedParameter : resolvedParameters) {
            switch (resolvedParameter.getParameterCategory()) {
                case JOINPOINT_PARAM:
                case MUTABLE_JOINPOINT_PARAM:
                case PROCEDDING_JOINPOINT_PARAM: {
                    // first parameter is Joinpoint
                    args[index++] = joinpoint;

                    break;
                }
                case STATIC_PART_PARAM: {
                    // first parameter is StaticPart
                    args[index++] = joinpoint.getStaticPart();

                    break;
                }
                case RETURNING_ANNOTATION: {
                    args[index++] = joinpoint.getReturnValue();

                    break;
                }
                case THROWING_ANNOTATION: {
                    args[index++] = joinpoint.getThrownException();

                    break;
                }
                case THIS_VAR:
                case TARGET_VAR: {
                    args[index++] = joinpoint.getTargetObject();

                    break;
                }
                case ARGS_VAR: {
                    args[index++] = joinpoint.getArguments()[resolvedParameter.getArgsIndex()];

                    break;
                }
                default:
                    break;
                }
        }
        return args;
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return adviceSpec.getAdviceClassName();
    }


    /**
     * {@link io.gemini.api.aop.Advice.Before} adapter that invokes the AspectJ before-advice
     * method via a cached {@link MethodHandle}.
     */
    static class BeforeAdviceAdapter extends AspectJAdviceAdapter<Object> implements Advice.Before<Object, Throwable> {

        private final MethodHandle beforeAdviceMethod;


        public BeforeAdviceAdapter(AspectJAdviceSpec.Loaded adviceSpec, Object delegateObject)
                throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
            super(adviceSpec, delegateObject);

            this.beforeAdviceMethod = getAdviceMethod(adviceSpec, delegateObject);
        }

        /**
         *  {@inheritDoc}
         */
        @Override
        public void before(MutableJoinpoint<Object, Throwable> joinpoint) throws Throwable {
            Object[] args = getArguments(joinpoint);
            beforeAdviceMethod.invokeWithArguments(args);
        }
    }


    /**
     * {@link io.gemini.api.aop.Advice.After} adapter that invokes the AspectJ after-advice
     * method via a cached {@link MethodHandle}.
     */
    static class AfterAdviceAdapter extends AspectJAdviceAdapter<Object> implements Advice.After<Object, Throwable> {

        private final MethodHandle afterAdviceMethod;


        public AfterAdviceAdapter(AspectJAdviceSpec.Loaded adviceSpec, Object delegateObject)
                throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
            super(adviceSpec, delegateObject);

            this.afterAdviceMethod = getAdviceMethod(adviceSpec, delegateObject);
        }

        /**
         *  {@inheritDoc}
         */
        @Override
        public void after(MutableJoinpoint<Object, Throwable> joinpoint) throws Throwable {
            Object[] args = getArguments(joinpoint);
            afterAdviceMethod.invokeWithArguments(args);
        }
    }
}