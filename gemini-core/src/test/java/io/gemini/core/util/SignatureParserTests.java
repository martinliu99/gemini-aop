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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.core.util.SignatureParser.MethodSignature;
import io.gemini.core.util.SignatureParser.TypeSignature;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription.InDefinedShape;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.pool.TypePool.CacheProvider;
import net.bytebuddy.pool.TypePool.Default.ReaderMode;

/**
 * Unit tests of {@code SignatureParser}.
 *
 * @author   martin.liu
 */
public class SignatureParserTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureParserTests.class);

    private TypePool typePool;

    @BeforeEach
    public void setup() {
        this.typePool = new TypePool.Default(CacheProvider.NoOp.INSTANCE, 
                ClassFileLocator.ForClassLoader.ofSystemLoader(), 
                ReaderMode.FAST
        );
    }


    @Test
    public void testFieldSignature() {
        TypeDescription typeDescription = typePool.describe(Param.class.getName()).resolve();

        FieldDescription fieldDescription1 = typeDescription.getDeclaredFields().filter( named("ls") ).getOnly();
        LOGGER.info("field1 Signature: {}", fieldDescription1.getGenericSignature());
        compareFieldSignature(fieldDescription1);

        FieldDescription fieldDescription2 = typeDescription.getDeclaredFields().filter( named("cmap") ).getOnly();
        LOGGER.info("field2 Signature: {}", fieldDescription2.getGenericSignature());
        compareFieldSignature(fieldDescription2);
    }

    private void compareFieldSignature(FieldDescription fieldDescription) {
        TypeSignature typeSignature = SignatureParser.parseTypeSignature(fieldDescription.getGenericSignature());
        Generic genericFeildType = fieldDescription.getType();
        TypeDescription erasureFieldType = genericFeildType.asErasure();

        assertThat(genericFeildType.getSort()).isEqualTo(typeSignature.getSort());
        assertThat(erasureFieldType.getTypeName()).isEqualTo(typeSignature.getTypeName());
    }


    @Test
    public void testMethodSignature() {
        TypeDescription typeDescription = typePool.describe(Clazz.class.getName()).resolve();
        MethodDescription.InDefinedShape methodDescription = typeDescription.getDeclaredMethods().filter( named("get") ).getOnly();
        LOGGER.info("methodSignature: {}", methodDescription.getGenericSignature());

        compareMethodSignature(methodDescription);
    }

    private void compareMethodSignature(MethodDescription.InDefinedShape methodDescription) {
        ParameterList<InDefinedShape> parameters = methodDescription.getParameters();
        MethodSignature erasureMethodSignature = SignatureParser.parseMethodSignature(methodDescription.getDescriptor());
        MethodSignature genericMethodSignature = SignatureParser.parseMethodSignature(methodDescription.getGenericSignature());
        for (int i = 0; i > parameters.size(); i++) {
            Generic genericParameterType = parameters.get(i++).getType();
            TypeDescription erasureParameterType = genericParameterType.asErasure();

            assertThat(erasureMethodSignature.getParameters().get(i).getTypeName()).isEqualTo(erasureParameterType.getTypeName());

            TypeSignature parameterSignature = genericMethodSignature.getParameters().get(i);
            assertThat(parameterSignature.getTypeName()).isEqualTo(erasureParameterType.getTypeName());

            assertThat(genericParameterType.getSort()).isEqualTo(genericParameterType.getSort());

            if (TypeDefinition.Sort.PARAMETERIZED == genericParameterType.getSort()) {
                TypeList.Generic typeArguments = genericParameterType.getTypeArguments();
                for (int j = 0; j < typeArguments.size(); j++) {
                    Generic genericArgumentType = typeArguments.get(i++);
                    TypeDescription erasureArgumentType = genericArgumentType.asErasure();

                    assertThat(parameterSignature.getTypeName()).isEqualTo(erasureArgumentType.getTypeName());
                }
            }
        }
    }


    static class Clazz<C> {

        @SuppressWarnings("rawtypes")
        public void get(Map<Param[][], List<Param[][]>> arg0, int arg1, long[][] arg2) {}
    }


    static class Param<C> {

        private List<C[][]> ls;
        private Map<String, ? extends Number> cmap;

        Map<String, ? extends Number> transform(List<C[][]> ls) {
            if (this.ls == ls)
                return cmap;
            else
                return null;
        }
    }
}
