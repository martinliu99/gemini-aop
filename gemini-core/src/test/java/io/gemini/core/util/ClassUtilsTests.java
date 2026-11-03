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
package io.gemini.core.util;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.pool.TypePool;

/**
 * Unit tests of {@code ClassUtils}.
 *
 * @author   martin.liu
 */
public class ClassUtilsTests {

    private static Logger LOGGER = LoggerFactory.getLogger(ClassUtilsTests.class);

    @Test
    public void name() {
        for (Class<?> clazz : Arrays.asList(int.class, int[].class, int[][][].class, String.class, String[][].class, List.class)) {
            TypeDescription type = TypeDescription.ForLoadedType.of(clazz);
            LOGGER.info("Class'{}' details: name '{}', descriptor '{}', type name '{}, canonical name '{}', internal name '{}', actual name '{}'", 
                    clazz, type.getName(), type.getDescriptor(), type.getTypeName(), type.getCanonicalName(), type.getInternalName(), type.getActualName());
        }
    }

    @Test
    public void forName() {
        for (Class<?> expectedType : new Class<?>[] {
            int.class, int[][][].class,
            String.class, String[].class,
            List.class,
        }) {
            String typeName = TypeDescription.ForLoadedType.of(expectedType).getTypeName();
            Class<?> loadedType = null;

            try {
                loadedType = ClassUtils.forName(typeName, false, null);
            } catch (Throwable t) {
                LOGGER.warn("Could not instantiate class '{}'.", typeName);
            }
            LOGGER.info("Type '{}' is loaded, '{}'.", typeName, loadedType);

            assertThat(loadedType).isEqualTo(expectedType);
        }
    }

    @Test
    public void isAssignableFrom() {
        TypePool typePool = TypePool.ClassLoading.of(this.getClass().getClassLoader());
        TypeDescription clazzType = typePool.describe(Clazz.class.getName()).resolve();

        Generic field01Generic = clazzType.getDeclaredFields().filter(named("field01")).getOnly().getType();
        Generic field02Generic = clazzType.getDeclaredFields().filter(named("field02")).getOnly().getType();
        Generic field03Generic = clazzType.getDeclaredFields().filter(named("field03")).getOnly().getType();

        // assignable
        assertThat(ClassUtils.isAssignableFrom(field02Generic, field01Generic, false)).isTrue();
        assertThat(ClassUtils.isAssignableFrom(field03Generic, field01Generic, false)).isTrue();

        assertThat(ClassUtils.isAssignableFrom(field01Generic, field02Generic, false)).isFalse();
        assertThat(ClassUtils.isAssignableFrom(field01Generic, field03Generic, false)).isFalse();

        assertThat(ClassUtils.isAssignableFrom(field02Generic, field03Generic, false)).isTrue();


        Generic field11Generic = clazzType.getDeclaredFields().filter(named("field11")).getOnly().getType();
        Generic field12Generic = clazzType.getDeclaredFields().filter(named("field12")).getOnly().getType();
        Generic field13Generic = clazzType.getDeclaredFields().filter(named("field13")).getOnly().getType();

        // assignable
        assertThat(ClassUtils.isAssignableFrom(field12Generic, field11Generic, false)).isTrue();
        assertThat(ClassUtils.isAssignableFrom(field13Generic, field11Generic, false)).isTrue();

        assertThat(ClassUtils.isAssignableFrom(field11Generic, field12Generic, false)).isFalse();
        assertThat(ClassUtils.isAssignableFrom(field11Generic, field13Generic, false)).isFalse();

        assertThat(ClassUtils.isAssignableFrom(field12Generic, field13Generic, false)).isTrue();


        Generic field21Generic = clazzType.getDeclaredFields().filter(named("field21")).getOnly().getType();
        Generic field22Generic = clazzType.getDeclaredFields().filter(named("field22")).getOnly().getType();
        Generic field23Generic = clazzType.getDeclaredFields().filter(named("field23")).getOnly().getType();

        // assignable
        assertThat(ClassUtils.isAssignableFrom(field22Generic, field21Generic, false)).isFalse();
        assertThat(ClassUtils.isAssignableFrom(field23Generic, field21Generic, false)).isFalse();

        assertThat(ClassUtils.isAssignableFrom(field23Generic, field22Generic, false)).isFalse();

        Generic field25Generic = clazzType.getDeclaredFields().filter(named("field25")).getOnly().getType();
        Generic field26Generic = clazzType.getDeclaredFields().filter(named("field26")).getOnly().getType();

        assertThat(ClassUtils.isAssignableFrom(field25Generic, field21Generic, false)).isTrue();
        assertThat(ClassUtils.isAssignableFrom(field26Generic, field21Generic, false)).isFalse();

        assertThat(ClassUtils.isAssignableFrom(field25Generic, field22Generic, false)).isTrue();
        assertThat(ClassUtils.isAssignableFrom(field26Generic, field22Generic, false)).isTrue();

        assertThat(ClassUtils.isAssignableFrom(field25Generic, field23Generic, false)).isTrue();
        assertThat(ClassUtils.isAssignableFrom(field26Generic, field23Generic, false)).isFalse();

    }


    static class Clazz<T extends Number> {

        T field01;
        Object field02;
        Number field03;

        T[][] field11;
        Object[][] field12;
        Number[][] field13;

        Clazz<T> field21;
        Clazz<Number> field22;
        Clazz<Integer> field23;

        Clazz<? extends Number> field25;
        Clazz<? super Number> field26;
    }
}
