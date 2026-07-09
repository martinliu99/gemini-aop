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

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.factory.AdvisorContext;
import io.gemini.aop.factory.FactoryContext;
import io.gemini.aop.factory.support.AdviceSpec.AspectJAdviceSpec;
import io.gemini.aop.factory.support.AdvisorSpec.PointcutAdvisorSpec;
import io.gemini.aop.factory.support.PointcutSpec.AspectJPointcutSpec;
import io.gemini.aop.factory.support.PointcutSpec.ExprPointcutSpec;
import io.gemini.aop.factory.support.PointcutSpec.PojoPointcutSpec;
import io.gemini.aop.matcher.ExprPointcut;
import io.gemini.aop.matcher.ExprPointcut.AspectJExprPointcut;
import io.gemini.api.annotation.NoScanning;
import io.gemini.api.aop.Pointcut;
import io.gemini.aspectj.weaver.ExprParser;
import io.gemini.core.OrderComparator;
import io.gemini.core.util.StringUtils;
import io.gemini.core.util.Throwables;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;


/**
 * Factory interface for creating {@link io.gemini.api.aop.Pointcut} instances from
 * {@link PointcutSpec} descriptors.
 * <p>
 * Three concrete implementations handle the three pointcut styles:
 * <ul>
 *   <li>{@link PojoPointcutCreator} – instantiates a {@link io.gemini.api.aop.Pointcut} class</li>
 *   <li>{@link ExprPointcutCreator} – parses an AspectJ expression into an {@link io.gemini.aop.matcher.ExprPointcut}</li>
 *   <li>{@link AspectJPointcutCreator} – parses an AspectJ expression with parameter binding</li>
 * </ul>
 * The {@link Compound} implementation delegates to all registered creators in order.
 * </p>
 *
 * @author   martin.liu
 */
public interface PointcutCreator {

    Logger LOGGER = LoggerFactory.getLogger(PointcutCreator.class);


    /**
     * Creates and returns a {@link io.gemini.api.aop.Pointcut} from the given
     * {@link AdvisorSpec.PointcutAdvisorSpec}, or {@code null} if this creator does not
     * handle the spec type.
     *
     * @param advisorContext the advisor context for the target class loader
     * @param advisorSpec    the pointcut advisor spec to create a pointcut from
     * @return the created {@link io.gemini.api.aop.Pointcut}, or {@code null}
     */
    Pointcut create(AdvisorContext advisorContext, PointcutAdvisorSpec advisorSpec);

    /**
     * Delegates to all registered {@link PointcutCreator} instances in order,
     * and returns the first non-null result.
     */
    @NoScanning
    class Compound implements PointcutCreator {

        private final List<? extends PointcutCreator> pointcutCreators;


        public Compound(FactoryContext factoryContext, AdviceCreator adviceCreator) {
            List<? extends PointcutCreator> pointcutCreators = factoryContext.getObjectFactory()
                    .createObjectsImplementing(PointcutCreator.class, false, adviceCreator);
            this.pointcutCreators = pointcutCreators == null 
                    ? Collections.emptyList() : pointcutCreators;

            OrderComparator.sort(pointcutCreators);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Pointcut create(AdvisorContext advisorContext, PointcutAdvisorSpec advisorSpec) {
            for (PointcutCreator pointcutCreator : pointcutCreators) {
                try {
                    Pointcut pointcut = pointcutCreator.create(advisorContext, advisorSpec);
                    if (pointcut != null) 
                        return pointcut;
                } catch (IllegalSpecException e) {
                    throw e;
                } catch (Throwable t) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Could not create Pointcut via '{}'. \n"
                                + "  Error reason: {} \n", 
                                pointcutCreator.getClass().getSimpleName(), 
                                t.getMessage(), 
                                t
                        );

                    Throwables.throwIfRequired(t);
                }
            }
            return null;
        }
    }


    /**
     * Abstract base providing common pointcut creation logic: spec class filtering,
     * expression parsing error handling, and pointcut decoration with advice method matchers.
     */
    abstract class AbstractBase<AS extends AdviceSpec, PS extends PointcutSpec, P extends Pointcut> implements PointcutCreator {

        private final AdviceCreator adviceCreator;


        public AbstractBase(AdviceCreator adviceCreator) {
            this.adviceCreator = adviceCreator;
        }


        protected abstract boolean supports(PointcutAdvisorSpec advisorSpec);

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public Pointcut create(AdvisorContext advisorContext, PointcutAdvisorSpec advisorSpec) {
            AdviceSpec adviceSpec = advisorSpec.getAdviceSpec();
            PointcutSpec pointcutSpec = advisorSpec.getPointcutSpec();
            if (advisorSpec == null || adviceSpec == null || pointcutSpec == null || supports(advisorSpec) == false)
                return null;

            // 1.try to create Pointcut
            P pointcut = null;
            try {
                pointcut = doCreatePointcut(advisorContext, (AS) adviceSpec, (PS) pointcutSpec);
            } catch (IllegalSpecException e) {
                throw e;
            } catch (ExprParser.ExprParseException e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ignored AdvisorSpec with unparsable PointcutExpression. \n"
                            + "  AdvisorName:{} \n"
                            + "  PointcutExpression: {} \n"
                            + "  ClassLoader: {} \n"
                            + "  Syntax Error: {} \n", 
                            advisorSpec.getAdvisorName(), 
                            e.getExpression(), 
                            advisorContext.getTargetClassLoaderName(), 
                            e.getMessage()
                    );

                throw new IllegalSpecException();
            } catch (ExprParser.ExprLintException e) {
                if (advisorContext.isValidateContext() == false && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Ignored AdvisorSpec with lint PointcutExpression. \n"
                            + "  AdvisorName:{} \n"
                            + "  PointcutExpression: {} \n"
                            + "  ClassLoader: {} \n"
                            + "  Lint message: {} \n", 
                            advisorSpec.getAdvisorName(), 
                            e.getExpression(), 
                            advisorContext.getTargetClassLoaderName(), 
                            e.getMessage()
                    );
                }
            } catch (ExprParser.ExprUnknownException e) {
                if (advisorContext.isValidateContext() == false && LOGGER.isWarnEnabled()) {
                    Throwable cause = e.getCause();
                    LOGGER.warn("Ignored AdvisorSpec with illegal PointcutExpression. \n"
                            + "  AdvisorName:{} \n"
                            + "  PointcutExpression: {} \n"
                            + "  ClassLoader: {} \n"
                            + "  Error reason: {} \n", 
                            advisorSpec.getAdvisorName(), 
                            e.getExpression(), 
                            advisorContext.getTargetClassLoaderName(), 
                            cause.getMessage(), 
                            cause
                    );
                }
            } catch (Exception e) {
                if (advisorContext.isValidateContext() == false && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Ignored AdvisorSpec with illegal PointcutExpression. \n"
                            + "  AdvisorName:{} \n"
                            + "  ClassLoader: {} \n"
                            + "  Error reason: {} \n", 
                            advisorSpec.getAdvisorName(), 
                            advisorContext.getTargetClassLoaderName(), 
                            e.getMessage(), 
                            e
                    );
                }
            }

            if (pointcut == null) 
                return null;

            // 2.validate Pointcut
            try {
                if (this.doValidatePointcut(advisorContext, pointcut) == false)
                    return null;
            } catch (IllegalSpecException e) {
                throw e;
            } catch (Exception e) {
                if (advisorContext.isValidateContext() == false && LOGGER.isWarnEnabled())
                    LOGGER.warn("Ignored AdvisorSpec with illegal Pointcut. \n"
                            + "  AdvisorName:{} \n"
                            + "  ClassLoader: {} \n"
                            + "  Error reason: {} \n", 
                            advisorSpec.getAdvisorName(), 
                            advisorContext.getTargetClassLoaderName(), 
                            e.getMessage(), 
                            e
                    );
            }

            // 3.decorate Pointcut
            ElementMatcher<MethodDescription> methodMatcher = pointcut.getMethodMatcher();
            ElementMatcher<MethodDescription> adviceMethodMatcher = adviceCreator.createAdviceMatcher(
                    advisorContext, adviceSpec);
            if (adviceMethodMatcher != null)
                // match [methodMatcher, adviceMethodMatcher] sequentially
                methodMatcher = new ElementMatcher.Junction.Conjunction<MethodDescription>(
                        methodMatcher, adviceMethodMatcher );

            return new Pointcut.Default( 
                    pointcut.getTypeMatcher(), methodMatcher);
        }

        protected abstract P doCreatePointcut(AdvisorContext advisorContext, AS adviceSpec, PS pointcutSpec);

        protected boolean doValidatePointcut(AdvisorContext advisorContext, Pointcut pointcut) {
            return true;
        }
    }


    /**
     * Instantiates a {@link io.gemini.api.aop.Pointcut} class referenced by a
     * {@link io.gemini.aop.factory.support.PointcutSpec.PojoPointcutSpec}.
     */
    class PojoPointcutCreator extends AbstractBase<AdviceSpec, PojoPointcutSpec, Pointcut> {

        public PojoPointcutCreator(AdviceCreator adviceCreator) {
            super(adviceCreator);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean supports(PointcutAdvisorSpec advisorSpec) {
            return PojoPointcutSpec.class.isAssignableFrom( advisorSpec.getPointcutSpec().getClass() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Pointcut doCreatePointcut(AdvisorContext advisorContext, AdviceSpec adviceSpec, PojoPointcutSpec pointcutSpec) {
            return advisorContext.getObjectFactory().createObject(
                    pointcutSpec.getPointcutClass() );
        }
    }


    /**
     * Creates an {@link io.gemini.aop.matcher.ExprPointcut.AspectJExprPointcut} from an
     * {@link io.gemini.aop.factory.support.PointcutSpec.ExprPointcutSpec} expression string.
     */
    class ExprPointcutCreator extends AbstractBase<AdviceSpec, ExprPointcutSpec, Pointcut> {

        public ExprPointcutCreator(AdviceCreator adviceCreator) {
            super(adviceCreator);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean supports(PointcutAdvisorSpec advisorSpec) {
            return ExprPointcutSpec.class.isAssignableFrom( advisorSpec.getPointcutSpec().getClass() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Pointcut doCreatePointcut(AdvisorContext advisorContext, AdviceSpec adviceSpec, ExprPointcutSpec pointcutSpec) {
            String pointcutExpression = pointcutSpec.getPointcutExpression();
            if (StringUtils.hasText(pointcutExpression) == false) {
                return null;
            }

            // try to replace placeholders in expression
            if (pointcutExpression != null)
                pointcutExpression = advisorContext.getPlaceholderHelper().replace(pointcutExpression);

            return new AspectJExprPointcut(advisorContext.getTypeWorld(), pointcutExpression);
        }
    }


    /**
     * Creates an {@link io.gemini.aop.matcher.ExprPointcut.AspectJExprPointcut} with parameter
     * binding from an {@link io.gemini.aop.factory.support.PointcutSpec.AspectJPointcutSpec}.
     */
    class AspectJPointcutCreator extends AbstractBase<AspectJAdviceSpec.Loaded, AspectJPointcutSpec, ExprPointcut> {

        public AspectJPointcutCreator(AdviceCreator adviceCreator) {
            super(adviceCreator);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean supports(PointcutAdvisorSpec advisorSpec) {
            return AspectJPointcutSpec.class.isAssignableFrom( advisorSpec.getPointcutSpec().getClass() )
                    && AspectJAdviceSpec.Loaded.class.isAssignableFrom( advisorSpec.getAdviceSpec().getClass() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ExprPointcut doCreatePointcut(AdvisorContext advisorContext, AspectJAdviceSpec.Loaded adviceSpec, 
                AspectJPointcutSpec pointcutSpec) {
            String pointcutExpression = pointcutSpec.getPointcutExpression();
            if (StringUtils.hasText(pointcutExpression) == false) {
                return null;
            }

            // try to replace placeholders in expression
            if (pointcutExpression != null)
                pointcutExpression = advisorContext.getPlaceholderHelper().replace(pointcutExpression);

            return new AspectJExprPointcut(advisorContext.getTypeWorld(), 
                    pointcutExpression,
                    adviceSpec.getDeclaringTypeName(), 
                    adviceSpec.getPointcutParameters(),
                    adviceSpec);
        }
    }
}
