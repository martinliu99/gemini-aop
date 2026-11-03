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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
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

//    @Test
    public void test() {
        for (Field field : Clazz.class.getDeclaredFields()) {
            LOGGER.info("{}:", field.getName());

            Class<?> type = field.getType();
            LOGGER.info("  name: {}", type.getName());
            LOGGER.info("  TypeName: {}", type.getTypeName());
            LOGGER.info("  CanonicalName: {}", type.getCanonicalName());
            LOGGER.info("  SimpleName: {}", type.getSimpleName());

            Type type2 = field.getGenericType();
            LOGGER.info("  TypeName: {}", type2.getTypeName());
        }

        TypeDescription typeDescription = typePool.describe(Clazz.class.getName()).resolve();
        for (FieldDescription field : typeDescription.getDeclaredFields()) {
            LOGGER.info("{}: {}", field.getName(), field.getType().getSort());

            Generic type = field.getType();
            LOGGER.info("  ActualName: {}", type.getActualName());
            LOGGER.info("  TypeName: {}", type.getTypeName());

            TypeDescription type2 = field.getType().asErasure();
            LOGGER.info("  name: {}", type2.getName());
            LOGGER.info("  ActualName: {}", type2.getActualName());
            LOGGER.info("  TypeName: {}", type2.getTypeName());
            LOGGER.info("  CanonicalName: {}", type2.getCanonicalName());
            LOGGER.info("  InternalName: {}", type2.getInternalName());
            LOGGER.info("  LongSimpleName: {}", type2.getLongSimpleName());
            LOGGER.info("  SimpleName: {}", type2.getSimpleName());
            LOGGER.info("  Descriptor: {}", type2.getDescriptor());
            LOGGER.info("  GenericSignature: {}", type2.getGenericSignature());

            net.bytebuddy.jar.asm.Type type3 = net.bytebuddy.jar.asm.Type.getType(field.getDescriptor());
            LOGGER.info("  ClassName: {}", type3.getClassName());
        }
        LOGGER.info("\n\n\n");
    }


    @Test
    public void testFieldSignature() {
        TypeDescription typeDescription = typePool.describe(Clazz.class.getName()).resolve();
        for (FieldDescription field : typeDescription.getDeclaredFields()) {
            compareFieldSignature(field);
        }
    }

    private void compareFieldSignature(FieldDescription fieldDescription) {
        Generic genericFeildType = fieldDescription.getType();

        String fieldDescriptor = fieldDescription.getGenericSignature();
        if (fieldDescriptor == null)
            fieldDescriptor = fieldDescription.getDescriptor();

        TypeSignature typeSignature = SignatureParser.parseTypeSignature(fieldDescriptor);

        LOGGER.info("Origin - {}: {}, {}", fieldDescription.getName(), genericFeildType.getTypeName(), genericFeildType.getSort());
        LOGGER.info("Parsed - {}: {}, {}", fieldDescription.getName(), typeSignature.getGenericName(), typeSignature.getSort());
        LOGGER.info("Generic Descriptor: {}", fieldDescriptor);

        if (TypeDefinition.Sort.PARAMETERIZED == genericFeildType.getSort()) {
            for (Generic argument : genericFeildType.getTypeArguments()) {
                LOGGER.info("  Argument: {}, {}", argument.getSort(), argument.getTypeName());
            }
        }

        assertThat(typeSignature.getSort()).isEqualTo(genericFeildType.getSort());

        if (TypeDefinition.Sort.VARIABLE != typeSignature.getSort() && TypeDefinition.Sort.GENERIC_ARRAY != typeSignature.getSort()) {
            TypeDescription erasureFieldType = genericFeildType.asErasure();
            assertThat(typeSignature.getTypeName()).isEqualTo(erasureFieldType.getTypeName());
        }

        List<? extends TypeSignature> typeArguments = typeSignature.getTypeArguments();
        if (typeArguments.size() == 0)
            return;

        int index = 0;
        for (TypeSignature typeArgument : typeArguments) {
            assertThat(typeArgument.getSort()).isEqualTo(genericFeildType.getTypeArguments().get(index).getSort());

            if (typeArgument.getSort() == TypeDefinition.Sort.NON_GENERIC || typeArgument.getSort() == TypeDefinition.Sort.PARAMETERIZED)
                assertThat(typeArgument.getTypeName()).isEqualTo(genericFeildType.getTypeArguments().get(index).asErasure().getTypeName());
            else if (typeArgument.getSort() == TypeDefinition.Sort.WILDCARD)
                assertThat(typeArgument.getTypeName()).isEqualTo(genericFeildType.getTypeArguments().get(index).getTypeName());

            index++;
        }
    }


    @Test
    public void testMethodSignature() {
        TypeDescription typeDescription = typePool.describe(Clazz.class.getName()).resolve();
        MethodDescription.InDefinedShape methodDescription = typeDescription.getDeclaredMethods().filter( named("get") ).getOnly();
        LOGGER.info("methodSignature: {}", methodDescription.getGenericSignature());

        for (MethodDescription method : typeDescription.getDeclaredMethods()) {
            LOGGER.info("{}:", method.getName());

            net.bytebuddy.jar.asm.Type methods = net.bytebuddy.jar.asm.Type.getMethodType(method.getDescriptor());
            for (net.bytebuddy.jar.asm.Type arg : methods.getArgumentTypes()) {
                LOGGER.info("  arg: {}", arg.getClassName());
            }
        }
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


    static class Clazz<C extends Number> {

        // NON_GENERIC
        public String field01;

        @SuppressWarnings("rawtypes")
        public List field02;

        public String[] field05;
        public String[][] field06;

        @SuppressWarnings("rawtypes")
        public List[] field07;
        @SuppressWarnings("rawtypes")
        public List[][] field08;


        // TYPE_VARIABLE
        public C field11;


        // GENERIC_ARRAY
        public List<String>[] field21;
        public List<String>[][] field22;

        public C[] field23;
        public List<C>[] field24;


        // PARAMETRIZED
        public List<String> field31;
        public List<String[]> field32;

        public List<C> field33;
        public List<C[]> field34;


        // WILDCARD
        public List<? super Number> field41;
        public List<List<? super Number>> field42;

        public List<? super C> field43;


        @SuppressWarnings("rawtypes")
        public void get(Map<Param[][], List<Param[][]>> arg0, int arg1, long[][] arg2) {}
    }


    static class Param<C> {

        private List<C[][]> field1;
        private Map<String, ? extends Number> field3;

        Map<String, ? extends Number> transform(List<C[][]> ls) {
            if (this.field1 == ls)
                return field3;
            else
                return null;
        }
    }
}
