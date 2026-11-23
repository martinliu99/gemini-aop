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

import io.gemini.aspectj.weaver.PointcutParameter;
import net.bytebuddy.description.type.TypeDescription.Generic;

/**
 * Describes a resolved parameter of advice method.
 */
interface ResolvedParameter extends PointcutParameter {

    /**
     * Returns the name of this parameter as declared in the pointcut expression.
     *
     * @return the parameter name; never {@code null}
     */
    String getParameterName();

    /**
     * {@inheritDoc}
     */
    @Override
    ParameterCategory getParameterCategory();

    /**
     * {@inheritDoc}
     */
    @Override
    String getMacthedTypeName();

    /**
     * Returns the type of this parameter.
     *
     * @return the parameter's` type; never {@code null}
     */
    Generic getMatchedType();

    /**
     * Returns the type bound to advice method.
     * 
     * @return the advice method's bound type
     */
    Generic getParameterType();

    /**
     * {@inheritDoc}
     */
    @Override
    int getArgsIndex();


    /**
     * Default implementation of {@code NamedPointcutParameter}.
     */
    class Default implements ResolvedParameter {

        private final String parameterName;

        private final ParameterCategory parameterCategory;

        private final Generic matchedType;
        private final Generic parameterType;

        private final int argsIndex;


        /**
         * Creates a {@code Default} parameter with no args index (set to {@link #INVALID_ARGS_INDEX}).
         *
         * @param parameterName     the parameter name
         * @param parameterCategory the parameter category
         * @param matchedType     the matched type of the target method
         * @param parameterType  the parameter type of the advice method
         */
        public Default(String parameterName, ParameterCategory parameterCategory, 
                Generic matchedType, Generic parameterType) {
            this.parameterName = parameterName;
            this.parameterCategory = parameterCategory;

            this.matchedType = matchedType;
            this.parameterType = parameterType;

            this.argsIndex = INVALID_ARGS_INDEX;
        }

        /**
         * Creates a {@code Default} parameter by copying the category and args index
         * from an existing {@link PointcutParameter}.
         *
         * @param parameterName  the parameter name
         * @param pointcutParameter  the source parameter to copy category and args index from
         * @param matchedType     the matched type of the target method
         * @param parameterType  the parameter type of the advice method
         */
        public Default(String parameterName, PointcutParameter pointcutParameter, 
                Generic matchedType, Generic parameterType) {
            this.parameterName = parameterName;
            this.parameterCategory = pointcutParameter.getParameterCategory();

            this.matchedType = matchedType;
            this.parameterType = parameterType;

            this.argsIndex = pointcutParameter.getArgsIndex();
        }

        /** 
         * {@inheritDoc} 
         */
        @Override
        public String getParameterName() {
            return this.parameterName;
        }

        /**
         *  {@inheritDoc} 
         */
        @Override
        public ParameterCategory getParameterCategory() {
            return this.parameterCategory;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public String getMacthedTypeName() {
            return matchedType.getTypeName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Generic getMatchedType() {
            return matchedType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Generic getParameterType() {
            return parameterType;
        }

        /**
         *  {@inheritDoc} 
         */
        @Override
        public int getArgsIndex() {
            return argsIndex;
        }
    }
}