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

import java.lang.annotation.Annotation;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;

/**
 * Finds {@link AnnotationDescription}.
 *
 * @author   martin.liu
 */
public interface AnnotationFinder {

    /**
     * Returns {@link AnnotationDescription} for the given Annotation class.
     * 
     * @param annotationClass the annotation class
     * @return  the {@link AnnotationDescription}
     */
    AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass);


    /**
     * The default implementation of {@link AnnotationFinder}.
     * 
     * Finds {@link AnnotationDescription} from given {@link AnnotationList}.
     */
    class Default implements AnnotationFinder {

        private final AnnotationList annotations;


        public Default(AnnotationList annotations) {
            this.annotations = annotations;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
            return annotations.ofType(annotationClass);
        }
    }
}