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

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.Advisor;
import io.gemini.aop.AopContext;
import io.gemini.aop.weaver.AopWeaver;
import io.gemini.api.annotation.NoScanning;
import io.gemini.core.OrderComparator;
import io.gemini.core.util.MethodUtils;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;

/**
 * Weaves one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances into a target method
 * via ByteBuddy bytecode transformation.
 * <p>
 * Two concrete implementations handle different advice styles:
 * <ul>
 *   <li>{@link RegularMethodWeaver} – weaves regular java method with {@link PointcutAdvisorHandler}</li>
 *   <li>{@link NativeMethodWeaver} – weaves native method with {@link PointcutAdvisorHandler}</li>
 * </ul>
 * The {@link Compound} implementation delegates to all registered weavers in order.
 * </p>
 *
 * @author   martin.liu
 */
public interface PointcutAdvisorWeaver {

    Logger LOGGER = LoggerFactory.getLogger(PointcutAdvisorWeaver.class);


    /**
     * Applies bytecode transformations for one or more advisors to the given type builder.
     *
     * @param builder   the ByteBuddy type builder to transform
     * @param aopWeaver the instance of {@link AopWeaver}
     * @param loadedType the type is loaded or not
     * @param targetMethod the target method to transform
     * @param advisors list of advisors
     * @return the modified type builder, or {@code null} if no transformation was applied
     */
    DynamicType.Builder<?> weave(DynamicType.Builder<?> builder, AopWeaver aopWeaver,
            boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors);


    /**
     * Delegates weaving to all registered {@link PointcutAdvisorWeaver} instances in order,
     * merging their bytecode transformations into the target type builder.
     */
    @NoScanning
    class Compound implements PointcutAdvisorWeaver {

        private final List<? extends PointcutAdvisorWeaver> methodWeavers;


        public Compound(AopContext aopContext) {
            List<? extends PointcutAdvisorWeaver> methodWeavers = aopContext.getObjectFactory()
                    .createObjectsImplementing(PointcutAdvisorWeaver.class, false);
            this.methodWeavers = methodWeavers == null 
                    ? Collections.emptyList() : methodWeavers;

             OrderComparator.sort(this.methodWeavers);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public DynamicType.Builder<?> weave(DynamicType.Builder<?> builder, AopWeaver aopWeaver,
                boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors) {
            for (PointcutAdvisorWeaver methodWeaver : methodWeavers) {
                try {
                    DynamicType.Builder<?> returnedBuilder = methodWeaver.weave(builder, aopWeaver,
                            loadedType, targetMethod, advisors);
                    if (returnedBuilder != null)
                        builder = returnedBuilder;
                } catch (Throwable t) {
                    if (LOGGER.isWarnEnabled()) 
                        LOGGER.warn("Could not weave target method via '{}'. \n"
                                + "  TargetMethod: {}"
                                + "  Error reason: {} \n", 
                                methodWeaver,
                                MethodUtils.getMethodSignature(targetMethod),
                                t.getMessage(),
                                t
                        );
                }
            }
            return builder;
        }
    }


    /**
     * Abstract base providing common logic.
     *
     */
    abstract class AbstractBase implements PointcutAdvisorWeaver {

        /** 
         * {@inheritDoc}
         */
        @Override
        public DynamicType.Builder<?> weave(DynamicType.Builder<?> builder, AopWeaver aopWeaver, 
                boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors) {
            List<? extends PointcutAdvisorHandler> advisorHandlers = aopWeaver.getWeaverContext().getAopContext().getObjectFactory()
                    .createObjectsImplementing(
                            PointcutAdvisorHandler.class, false,
                            aopWeaver, targetMethod, advisors);
            advisorHandlers = advisorHandlers == null 
                    ? Collections.emptyList() : advisorHandlers;

            OrderComparator.sort(advisorHandlers);

            return doWeave(builder, aopWeaver, loadedType, targetMethod, advisors, advisorHandlers);
        }

        protected abstract DynamicType.Builder<?> doWeave(DynamicType.Builder<?> builder, AopWeaver aopWeaver, 
                boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors, 
                List<? extends PointcutAdvisorHandler> advisorHandlers);
    }


    /**
     * Weaves one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances into a regular java method.
     */
    class RegularMethodWeaver extends AbstractBase {

        /** 
         * {@inheritDoc}
         */
        @Override
        protected DynamicType.Builder<?> doWeave(DynamicType.Builder<?> builder, AopWeaver aopWeaver, 
                boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors,
                List<? extends PointcutAdvisorHandler> advisorHandlers) {
            if (targetMethod.isNative())
                return builder;

            AsmVisitorWrapper asmVisitorWrapper = null;
            for (PointcutAdvisorHandler advisorHandler : advisorHandlers) {
                AsmVisitorWrapper combinedAsmVisitorWrapper = advisorHandler.combine(asmVisitorWrapper);
                if (combinedAsmVisitorWrapper != null)
                    asmVisitorWrapper = combinedAsmVisitorWrapper;
            }

            if (asmVisitorWrapper != null)
                builder = builder.visit(asmVisitorWrapper);

            return builder;
        }
    }


    /**
     * Weaves one or more {@link io.gemini.aop.Advisor.PointcutAdvisor} instances into a native method.
     */
    class NativeMethodWeaver extends AbstractBase {

        /** 
         * {@inheritDoc}
         */
        @Override
        protected DynamicType.Builder<?> doWeave(DynamicType.Builder<?> builder, AopWeaver aopWeaver, 
                boolean loadedType, MethodDescription targetMethod, List<? extends Advisor> advisors,
                List<? extends PointcutAdvisorHandler> advisorHandlers) {
            if (!targetMethod.isNative())
                return builder;
            else if (loadedType) {
                LOGGER.warn("Could not retransform type '{}' loaded by ClassLoader '{}' in AopWeaver, \n"
                        + "  NativeTargetMethod: {}",
                        targetMethod.getDeclaringType().getTypeName(),
                        targetMethod.toGenericString());
                return builder;
            }

            AsmVisitorWrapper asmVisitorWrapper = null;
            Implementation implementation = null;
            for (PointcutAdvisorHandler advisorHandler : advisorHandlers) {
                AsmVisitorWrapper visitorWrapper = advisorHandler.combine(asmVisitorWrapper);
                if (visitorWrapper != null)
                    asmVisitorWrapper = visitorWrapper;

                Implementation combinedImplementation = advisorHandler.combine(implementation);
                if(combinedImplementation != null)
                    implementation = combinedImplementation;
            }

            if (asmVisitorWrapper != null)
                builder = builder.visit(asmVisitorWrapper);
            if (implementation != null)
                builder = builder.invokable( is(targetMethod) ).intercept(implementation);

            return builder;
        }
    }
}