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
package io.gemini.aspectj.weaver;

/**
 * Describes a single parameter in an AspectJ pointcut binding.
 * <p>
 * Each parameter has a category ({@link ParameterCategory}) indicating its role
 * (e.g., joinpoint, returned value, args binding), an optional argument index,
 * and — for named parameters — a name and type.
 * </p>
 *
 * @author   martin.liu
 */
public interface PointcutParameter {

    int INVALID_ARGS_INDEX = -1;


    /**
     * Returns the category of this pointcut parameter, indicating its role
     * (e.g., joinpoint, args binding, annotation variable).
     *
     * @return the parameter category; never {@code null}
     */
    ParameterCategory getParameterCategory();

    /**
     * Returns the type name of this pointcut parameter.
     * 
     * @return the type name of parameter.
     */
    String getMacthedTypeName();

    /**
     * Returns the index of this parameter in the method's argument list,
     * or {@link #INVALID_ARGS_INDEX} if not applicable.
     *
     * @return the argument index, or {@code -1} if not an args-bound parameter
     */
    int getArgsIndex();


    /**
     * Enumerates parameter categories.
     */
    enum ParameterCategory {

        JOINPOINT_PARAM,
        MUTABLE_JOINPOINT_PARAM,
        PROCEDDING_JOINPOINT_PARAM,
        STATIC_PART_PARAM,

        RETURNING_ANNOTATION,
        THROWING_ANNOTATION,

        THIS_VAR,
        TARGET_VAR,
        ARGS_VAR,
        AT_THIS_VAR,
        AT_TARGET_VAR,
        AT_ARGS_VAR,
        AT_WITHIN_VAR,
        AT_WITHINCODE_VAR,
        AT_ANNOTATION_VAR,
        ;
    }
}
