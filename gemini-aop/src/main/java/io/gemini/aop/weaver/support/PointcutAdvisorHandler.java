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
package io.gemini.aop.weaver.support;

import static net.bytebuddy.matcher.ElementMatchers.is;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.AdviceKind.ByteBuddyAdviceKind;
import io.gemini.aop.AdviceKind.Managed;
import io.gemini.aop.Advisor;
import io.gemini.aop.Advisor.PointcutAdvisor;
import io.gemini.aop.weaver.AopWeaver;
import io.gemini.aop.weaver.Joinpoints;
import io.gemini.aop.weaver.advice.CircularityBreakerCodeGenerator;
import io.gemini.aop.weaver.advice.DescriptorOffset;
import io.gemini.api.annotation.Order;
import io.gemini.core.OrderComparator;
import io.gemini.core.object.ObjectFactory.ObjectsException;
import io.gemini.core.util.ClassUtils;
import io.gemini.core.util.CollectionUtils;
import io.gemini.core.util.MethodHandleLookup;
import io.gemini.core.util.Throwables;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.WithCustomMapping;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods;
import net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.pool.TypePool.CacheProvider;
import net.bytebuddy.pool.TypePool.Default.ReaderMode;

/**
 * Combines one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances to an instance of
 * {@link net.bytebuddy.asm.AsmVisitorWrapper} or {@link net.bytebuddy.implementation.Implementation},
 * and registers byte code callback. 
 * <p>
 * Two concrete implementations handle different advice styles:
 * <ul>
 *   <li>{@link ManagedAdviceHandler} – combines POJO and AspectJ advice using framework-generated
 *       {@code io.gemini.api.aop.Advice} classes, and handles callback with the 
 *       {@link io.gemini.aop.weaver.BootstrapDispatcher}.</li>
 *   <li>{@link ByteBuddyAdviceHandler} – combines raw ByteBuddy {@code net.bytebuddy.asm.Advice} classes.</li>
 * </ul>
 * </p>
 *
 * @author   martin.liu
 */
public interface PointcutAdvisorHandler {

    Logger LOGGER = LoggerFactory.getLogger(PointcutAdvisorHandler.class);


    /**
     * Combine one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances with the given
     * {@link net.bytebuddy.asm.AsmVisitorWrapper} instance.
     * 
     * @param asmVisitorWrapper the given {@link net.bytebuddy.asm.AsmVisitorWrapper} instance.
     * @return the combined {@link net.bytebuddy.asm.AsmVisitorWrapper} instance.
     */
    AsmVisitorWrapper combine(AsmVisitorWrapper asmVisitorWrapper);


    /**
     * Combine one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances with the given
     * {@link net.bytebuddy.implementation.Implementation} instance.
     * 
     * @param implementation the given {@link net.bytebuddy.implementation.Implementation} instance.
     * @return the combined {@link net.bytebuddy.implementation.Implementation} instance.
     */
    Implementation combine(Implementation implementation);


    /**
     * Abstract base providing common logic, such as resolving and sorting advisor classes, registering byte code
     * callback.
     *
     */
    abstract class AbstractBase implements PointcutAdvisorHandler {

        private final AopWeaver aopWeaver;
        private final MethodDescription targetMethod;
        private final List<? extends PointcutAdvisor> advisors;

        private int callbackSlot = 0;


        public AbstractBase(AopWeaver aopWeaver, MethodDescription targetMethod, List<? extends PointcutAdvisor> advisors) {
            this.aopWeaver = aopWeaver;
            this.targetMethod = targetMethod;

            this.advisors = resolveAdvisors(advisors);
            if (CollectionUtils.isEmpty(this.advisors))
                throw new ObjectsException.SkippedObjectCreationException();
        }

        protected List<? extends PointcutAdvisor> resolveAdvisors(List<? extends Advisor> advisors) {
            List<PointcutAdvisor> candidateAdvisors = new ArrayList<>();
            for (Advisor advisor : advisors) {
                if (advisor == null || !(advisor instanceof PointcutAdvisor))
                    continue;

                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                if (!(isCandidateAdvisor(pointcutAdvisor)))
                    continue;

                candidateAdvisors.add(pointcutAdvisor);
            }

            OrderComparator.sort(candidateAdvisors);

            return candidateAdvisors;
        }

        protected abstract boolean isCandidateAdvisor(PointcutAdvisor advisor);


        protected int registerCallback(AopWeaver.WeavedCodeCallback callback) {
            return this.callbackSlot = aopWeaver.registerCallback(callback);
        }

        public Object callback(Lookup targetLookup, String methodName, MethodType methodType, Object... arguments) {
            if (this.callbackSlot != 0) {
                int callbackSlot = (int) arguments[0];
                if (callbackSlot != this.getCallbackSlot())
                    return null;
            }

            return doCallback(targetLookup, methodName, methodType, arguments);
        }

        protected abstract Object doCallback(Lookup targetLookup, String methodName, MethodType mothodType, Object... arguments);


        protected AopWeaver getAopWeaver() {
            return aopWeaver;
        }

        protected MethodDescription getTargetMethod() {
            return targetMethod;
        }

        protected List<? extends PointcutAdvisor> getAdvisors() {
            return advisors;
        }

        protected int getCallbackSlot() {
            return callbackSlot;
        }
    }


    /**
     * Creates the framework-generated {@code io.gemini.api.aop.Advice} class with POJO and AspectJ advices,
     * and register byte code callback with the {@link io.gemini.aop.weaver.BootstrapDispatcher}.
     * Implements {@link AopWeaver.WeavedCodeCallback} to create the {@link io.gemini.aop.weaver.Joinpoints.Descriptor}
     * on first invocation and cache it for subsequent calls.
     */
    @Order(-100)
    class ManagedAdviceHandler extends AbstractBase implements AopWeaver.WeavedCodeCallback {

        private static final Map<Class<?>, DynamicType.Loaded<?>> BEARKING_CIRCULARITY_ADVICES = new HashMap<>();

        private Joinpoints.Descriptor descriptor;


        public ManagedAdviceHandler(AopWeaver aopWeaver, MethodDescription targetMethod, List<? extends PointcutAdvisor> advisors) {
            super(aopWeaver, targetMethod, advisors);

            registerCallback(this);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean isCandidateAdvisor(PointcutAdvisor advisor) {
            return Managed.class.isAssignableFrom(advisor.getAdviceKind().getClass());
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public AsmVisitorWrapper combine(AsmVisitorWrapper asmVisitorWrapper) {
            MethodDescription targetMethod = getTargetMethod();

            ForDeclaredMethods frameworkAdvice = toAdvice(
                    getAopWeaver().getWeaverContext().getFrameworkAdviceClass(targetMethod))
                    .on( is(targetMethod) );

            return asmVisitorWrapper == null 
                    ? frameworkAdvice
                    : new AsmVisitorWrapper.Compound(frameworkAdvice, asmVisitorWrapper);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public Implementation combine(Implementation implementation) {
            MethodDescription targetMethod = getTargetMethod();

            Advice frameworkAdvice = toAdvice(
                    getAopWeaver().getWeaverContext().getFrameworkAdviceClass(targetMethod));

            return implementation == null ? frameworkAdvice : frameworkAdvice.wrap(implementation);
        }

        private Advice toAdvice(Class<?> frameworkAdviceClass) {
            WithCustomMapping withCustomMapping = Advice.withCustomMapping().bind( 
                    DescriptorOffset.create(
                            getTargetMethod(), Integer.valueOf(getCallbackSlot()) ) );

            if (!isBreakCircularity(getAdvisors()))
                return withCustomMapping.to(frameworkAdviceClass);


            DynamicType.Loaded<?> wrappedAdviceType = transformAdvice(frameworkAdviceClass);

            return withCustomMapping.to(wrappedAdviceType.getTypeDescription(), wrappedAdviceType);
        }

        private boolean isBreakCircularity(List<? extends PointcutAdvisor> advisors) {
            for (PointcutAdvisor pointcutAdvisor : advisors) {
                if (pointcutAdvisor.isBreakCircularity())
                    return true;
            }
            return false;
        }

        private DynamicType.Loaded<?> transformAdvice(Class<?> frameworkAdviceClass) {
            DynamicType.Loaded<?> wrappedAdviceType = BEARKING_CIRCULARITY_ADVICES.computeIfAbsent(
                    frameworkAdviceClass, 
                    clazz -> CircularityBreakerCodeGenerator.INSTANCE.wrapMethodImplementation(clazz)
            );
            return wrappedAdviceType;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected Object doCallback(Lookup targetLookup, String methodName, MethodType mothodType, Object... arguments) {
            Joinpoints.Descriptor descriptor = doCreateJoinpointDescriptor(targetLookup);
            boolean isCallSite = arguments[1].equals(DescriptorOffset.CALLSITE_DESCRIPTOR_FLAG);
            if (!isCallSite)
                return descriptor;

            MethodHandle constant = MethodHandles.constant(Object.class, descriptor);
            return new ConstantCallSite( constant );
        }

        protected Joinpoints.Descriptor doCreateJoinpointDescriptor(Lookup targetLookup) {
            if (this.descriptor != null)
                return this.descriptor;

            Class<?> targetClass = targetLookup.lookupClass();
            ClassLoader targetClassLoader = targetClass.getClassLoader();


            MethodDescription targetMethod = getTargetMethod();
            String accessibleName = targetMethod.toGenericString();
            try {
                List<? extends PointcutAdvisor> advisors = getAdvisors();
                if (CollectionUtils.isEmpty(advisors))
                    return null;

                //
                AccessibleObject accessibleObject = null;
                if (!targetMethod.isTypeInitializer()) {
                    accessibleObject = ClassUtils.getAccessibleObject(targetClass, targetMethod);
                }

                return (this.descriptor = new Joinpoints.Descriptor(targetLookup, accessibleName, accessibleObject, advisors));
            } catch (Throwable t) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Could not create joinpoint descriptor for type '{}' loaded by ClassLoader '{}'. \n"
                            + "  TargetMethod: {}"
                            + "  Error reason: {} \n", 
                            targetMethod.getDeclaringType().getTypeName(), targetClassLoader, 
                            accessibleName, 
                            t.getMessage(),
                            t
                    );

                Throwables.throwIfRequired(t);
                return null;
            }
        }
    }


    /**
     * Creates raw ByteBuddy {@code net.bytebuddy.asm.Advice} classes, and uses the INDY bootstrap mechanism to 
     * route callbacks to the correct {@code ByteBuddyAdviceHandler}.
     */
    @Order(100)
    class ByteBuddyAdviceHandler extends AbstractBase implements AopWeaver.WeavedCodeCallback {

        private static final Map<Class<?>, DynamicType.Loaded<?>> BEARKING_CIRCULARITY_ADVICES = new HashMap<>();

        private final Map<String, Class<?>> adviceClasses;


        public ByteBuddyAdviceHandler(AopWeaver aopWeaver, MethodDescription targetMethod, List<? extends PointcutAdvisor> advisors) {
            super(aopWeaver, targetMethod, advisors);

            this.adviceClasses = new HashMap<>(this.getAdvisors().size());

            registerCallback(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean isCandidateAdvisor(PointcutAdvisor advisor) {
            return ByteBuddyAdviceKind.class.isAssignableFrom(advisor.getAdviceKind().getClass());
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public AsmVisitorWrapper combine(AsmVisitorWrapper asmVisitorWrapper) {
            List<? extends PointcutAdvisor> advisors = getAdvisors();

            List<MethodVisitorWrapper> methodVisitorWrappers = new ArrayList<>(advisors.size() + 1);
            methodVisitorWrappers.add( CircularityBreakerCodeGenerator.INSTANCE.postProcessTargetMethod(getCallbackSlot()) );

            if (!getTargetMethod().isNative()) {
                AopWeaver aopWeaver = getAopWeaver();
                for (Iterator<? extends PointcutAdvisor> it = advisors.iterator(); it.hasNext(); ) {
                    PointcutAdvisor advisor = it.next();

                    Advice advice = toAdvice(aopWeaver, advisor);
                    methodVisitorWrappers.add(advice);
                }
            }

            ForDeclaredMethods bytebuddyAdvice = new AsmVisitorWrapper.ForDeclaredMethods()
                    .invokable( is( getTargetMethod() ), methodVisitorWrappers);

            return asmVisitorWrapper == null 
                    ? bytebuddyAdvice
                    : new AsmVisitorWrapper.Compound(bytebuddyAdvice, asmVisitorWrapper);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public Implementation combine(Implementation implementation) {
            List<? extends PointcutAdvisor> advisors = getAdvisors();

            AopWeaver aopWeaver = getAopWeaver();
            for (ListIterator<? extends PointcutAdvisor> it = advisors.listIterator(advisors.size()); it.hasPrevious(); ) {
                PointcutAdvisor advisor = it.previous();

                Advice advice = toAdvice(aopWeaver, advisor)
                        .withAssigner(Assigner.DEFAULT)
                        .withExceptionHandler(Advice.ExceptionHandler.Default.SUPPRESSING);

                implementation = implementation == null ? advice : advice.wrap(implementation);
            }

            return implementation;
        }

        private Advice toAdvice(AopWeaver aopWeaver, PointcutAdvisor advisor) {
            WithCustomMapping withCustomMapping = Advice.withCustomMapping()
                    .with(new Advice.AssignReturned.Factory())
                    .bootstrap(
                            AopWeaver.BOOTSTRAP_DISPATCHER_CALLBACK_METHOD,
                            new DefaultBootstrapArgumentResolverFactory(getCallbackSlot())
                    );

            Class<?> originalAdviceClass = advisor.getAdviceClass();
            String originalAdviceClassName = originalAdviceClass.getName();

            // record original advice class for future MethodHandle resolving.
            this.adviceClasses.put(originalAdviceClassName, originalAdviceClass);

            ClassLoader classLoader = originalAdviceClass.getClassLoader();
            TypePool typePool = aopWeaver.getWeaverContext().getAopContext().getTypePoolFactory().createTypePool(classLoader, null);

            // create byte buddy advice with TypeDescription other Class to avoid resolving referred target object twice.
            TypeDescription originalAdviceType = typePool.describe(originalAdviceClassName).resolve();
            ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(classLoader);
            Advice byteBuddyAdvice = withCustomMapping.to(originalAdviceType, classFileLocator);

            if (advisor.isBreakCircularity()) {
                DynamicType.Loaded<?> transformedAdviceType = BEARKING_CIRCULARITY_ADVICES.computeIfAbsent(
                        originalAdviceClass, 
                        clazz -> CircularityBreakerCodeGenerator.INSTANCE.bridgeMethodCall(originalAdviceClass, originalAdviceType, classFileLocator)
                );

                TypePool transformedAdvicetypePool = new TypePool.Default(CacheProvider.NoOp.INSTANCE, transformedAdviceType, ReaderMode.FAST, typePool);
                String typeName = transformedAdviceType.getTypeDescription().getTypeName();

                // re-resolve TypeDescription other than getting getTypeDescription() from transformedAdviceType
                // since the latter method might differ from the binary representation of this type. 
                // Normally, annotations and intercepted methods are not added to this type description..
                byteBuddyAdvice = withCustomMapping.to(
                        transformedAdvicetypePool.describe(typeName).resolve(), 
                        transformedAdviceType);
            }

            return byteBuddyAdvice
                    .withExceptionHandler(Advice.ExceptionHandler.Default.SUPPRESSING);
        }


        /**
         * {@inheritDoc}
         * TODO: fail fast?
         */
        @Override
        protected Object doCallback(Lookup targetLookup, String methodName, MethodType mothodType, Object... arguments) {
            int callbackSlot = (int) arguments[0];
            if (callbackSlot != this.getCallbackSlot())
                return null;

            MethodHandle methodHandle = getMethodHandle((String) arguments[1], methodName, mothodType);
            if (methodHandle == null) {
                return null;
            }

            return new ConstantCallSite(methodHandle);
        }

        private MethodHandle getMethodHandle(String adviceTypeName, String adviceMethodName, MethodType adviceMethodType) {
            if (!adviceClasses.containsKey(adviceTypeName))
                return null; // throw?

            Class<?> byteBuddyAdvice = adviceClasses.get(adviceTypeName);

            try {
                MethodHandleLookup lookup = MethodHandleLookup.select(byteBuddyAdvice, MethodHandles.lookup());
                return lookup.findStatic(byteBuddyAdvice, adviceMethodName, adviceMethodType);
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Could not find static advice method. \n"
                            + "  TargetMethod: {} \n"
                            + "  AdviceMethod: {} {} \n"
                            + "  Error reason: {} \n", 
                            getTargetMethod().toGenericString(),
                            adviceMethodName, adviceMethodType,
                            e.getMessage(),
                            e
                    );
            }

            return null;
        }
    }
}