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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.jar.asm.signature.SignatureReader;
import net.bytebuddy.jar.asm.signature.SignatureVisitor;

/**
 * Resolving type, method or field signature with ASM {@code SignatureParser}.
 *
 * @author   martin.liu
 */
public final class SignatureParser {

    public static TypeSignature parseTypeSignature(String typeSignture) {
        TypeSignatureVisitor typeSignatureVisitor = new TypeSignatureVisitor();
        new SignatureReader(typeSignture).accept(typeSignatureVisitor);

        return typeSignatureVisitor;
    }


    public static MethodSignature parseMethodSignature(String methodSignture) {
        MethodSignatureVisitor methodSignatureVisitor = new MethodSignatureVisitor();
        new SignatureReader(methodSignture).accept(methodSignatureVisitor);

        return methodSignatureVisitor;
    }


    public interface TypeSignature {

        boolean containTypeVairable();

        TypeDefinition.Sort getSort();

        String getTypeName();

        String getGenericName();

        TypeSignature getComponentType();

        List<? extends TypeSignature> getTypeArguments();
    }


    public interface MethodSignature {

        List<? extends TypeSignature> getParameters();

        List<String> getParameterTypes();

        TypeSignature getReturnType();
    }


    protected static class MethodSignatureVisitor extends SignatureVisitor implements MethodSignature {

        private final List<TypeSignatureVisitor> parameterVisitors = new ArrayList<>();
        private List<String> parameterTypeNames;

        private TypeSignatureVisitor returnType;


        protected MethodSignatureVisitor() {
            super(Opcodes.ASM9);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public List<? extends TypeSignature> getParameters() {
            return parameterVisitors;
        }

        @Override
        public List<String> getParameterTypes() {
            if (this.parameterTypeNames == null)
                this.parameterTypeNames = getParameters().stream()
                        .map( e -> e.getTypeName() )
                        .collect( Collectors.toList() );

            return this.parameterTypeNames;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypeSignature getReturnType() {
            return returnType;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public SignatureVisitor visitParameterType() {
            TypeSignatureVisitor paramVisitor = new TypeSignatureVisitor();
            parameterVisitors.add(paramVisitor);
            return paramVisitor;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public SignatureVisitor visitReturnType() {
            this.returnType = new TypeSignatureVisitor();
            return this.returnType;
        }
    }


    protected static class TypeSignatureVisitor extends SignatureVisitor implements TypeSignature {

        private static final String ARRAY = "[]";

        private static final char ARRAY_SYMBOL = '[';
        private static final char WILDCARD_SYMBOL = '?';

        private TypeDefinition.Sort sort;
        private String typeName;
        private String genericName;

        private TypeSignatureVisitor componentType;
        private List<TypeSignatureVisitor> typeArgumentVisitors = new ArrayList<>();


        protected TypeSignatureVisitor() {
            this("");
        }

        protected TypeSignatureVisitor(String typeName) {
            super(Opcodes.ASM9);

            this.typeName = typeName;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public boolean containTypeVairable() {
            return false;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypeDefinition.Sort getSort() {
            if (sort == null) {
                sort = getComponentType().getSort() == TypeDefinition.Sort.NON_GENERIC
                        ? TypeDefinition.Sort.NON_GENERIC : TypeDefinition.Sort.GENERIC_ARRAY;
            }
            return sort;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public String getTypeName() {
            if ("".equals(typeName)) {
                TypeSignature componentType = getComponentType();
                if (componentType != null) {
                    String name = componentType.getTypeName();
                    if (name.charAt(0) != ARRAY_SYMBOL && !name.endsWith(ARRAY))
                        this.typeName = TypeDefinition.Sort.VARIABLE == componentType.getSort() 
                            ? name + ARRAY : ARRAY_SYMBOL + "L" + name + ";";
                    else
                        this.typeName = name.endsWith(ARRAY) ? name + "[]" : ARRAY_SYMBOL + name;
                }
            }
            return typeName;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public String getGenericName() {
            if (genericName == null) {
                StringBuilder sBuilder = new StringBuilder(
                        getComponentType() == null ? getTypeName() : componentType.getGenericName() + ARRAY);

                if (typeArgumentVisitors.size() != 0) {
                    sBuilder.append("<");
                    for (TypeSignature type : typeArgumentVisitors)
                        sBuilder.append(type.getGenericName()).append(", ");
                    sBuilder.delete(sBuilder.length() - 2, sBuilder.length())
                    .append(">");
                }

                this.genericName = sBuilder.toString();
            }
            return genericName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TypeSignature getComponentType() {
            return componentType;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public List<? extends TypeSignature> getTypeArguments() {
            return typeArgumentVisitors;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public SignatureVisitor visitArrayType() {
            return (componentType = new TypeSignatureVisitor());
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void visitBaseType(final char descriptor) {
            sort = TypeDefinition.Sort.NON_GENERIC;

            String desc = descriptor + "";
            typeName += typeName.length() == 0 ? Type.getType(desc).getClassName() : desc;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void visitClassType(String name) {
            name = name.replace("/", ".");

            char firstChar = typeName.length() == 0 ? '\0' : typeName.charAt(0);
            if (WILDCARD_SYMBOL == firstChar) {
                typeName +=  name;

                sort = TypeDefinition.Sort.WILDCARD;
            } else {
                typeName = name;

                sort = TypeDefinition.Sort.NON_GENERIC;
            }
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public void visitTypeVariable(final String name) {
            char firstChar = typeName.length() == 0 ? '\0' : typeName.charAt(0);
            if (WILDCARD_SYMBOL == firstChar) {
                typeName +=  name;

                sort = TypeDefinition.Sort.WILDCARD;
            } else {
                typeName = name;

                sort = TypeDefinition.Sort.VARIABLE;
            }
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public SignatureVisitor visitTypeArgument(char wildcard) {
            sort = TypeDefinition.Sort.PARAMETERIZED;

            String typeName = "";
            if (SignatureVisitor.EXTENDS == wildcard)
                typeName = "? extends ";
            else if (SignatureVisitor.SUPER == wildcard)
                typeName = "? super ";
            TypeSignatureVisitor typeVisitor = new TypeSignatureVisitor(typeName);
            typeArgumentVisitors.add(typeVisitor);

            return typeVisitor;
        }
    }
}
