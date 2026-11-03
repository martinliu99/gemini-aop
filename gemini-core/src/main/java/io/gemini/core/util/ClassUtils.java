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

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;

/**
 * Utility class for class and type operations used throughout the AOP framework.
 * <p>
 * Provides helpers for:
 * <ul>
 *   <li>Loading classes by name with primitive type support</li>
 *   <li>Abbreviating fully-qualified class names for display</li>
 *   <li>Resolving {@link AccessibleObject} (Method/Constructor) from ByteBuddy {@link MethodDescription}</li>
 *   <li>Assignability checks between loaded and ByteBuddy generic types, including primitive boxing</li>
 *   <li>Type visibility checks across class loaders</li>
 *   <li>Converting class names to resource paths</li>
 * </ul>
 * </p>
 *
 * @author   martin.liu
 */
public abstract class ClassUtils {

    /**
     * The file extension for a Java class file.
     */
    public static final String CLASS_FILE_EXTENSION = ".class";

    /** The package separator character: {@code '.'}. */
    public static final char PACKAGE_SEPARATOR = '.';

    public static final char RESOURCE_SPERATOR = '/';


    private static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP;

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP;
    private static final Map<TypeDescription, TypeDescription> PRIMITIVE_TYPE_DESCRIPTION_MAP;

    private static final Map<Class<?>, List<Class<?>>> WIDENED_PRIMITIVE_MAP;
    private static final Map<TypeDescription, List<TypeDescription>> WIDENED_PRIMITIVE_TYPE_DESCRIPTION_MAP;

    static {
        PRIMITIVE_WRAPPER_MAP = new LinkedHashMap<>();

        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);

        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);

        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);

        PRIMITIVE_WRAPPER_MAP.put(void.class, Void.class);


        PRIMITIVE_TYPE_MAP = PRIMITIVE_WRAPPER_MAP.entrySet().stream()
                .collect( 
                        Collectors.toMap( 
                                entry -> entry.getKey().getName(), 
                                entry -> entry.getKey()
                        )
                );


        PRIMITIVE_TYPE_DESCRIPTION_MAP = PRIMITIVE_WRAPPER_MAP.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> TypeDescription.ForLoadedType.of(entry.getKey()), 
                                entry -> TypeDescription.ForLoadedType.of(entry.getValue())
                        )
                );


        WIDENED_PRIMITIVE_MAP = new LinkedHashMap<>();
        WIDENED_PRIMITIVE_MAP.put(void.class, Arrays.asList(void.class));
        WIDENED_PRIMITIVE_MAP.put(boolean.class, Arrays.asList(boolean.class));
        WIDENED_PRIMITIVE_MAP.put(byte.class, Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(short.class, Arrays.asList(short.class, int.class, long.class, float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(char.class, Arrays.asList(char.class, int.class, long.class, float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(int.class, Arrays.asList(int.class, long.class, float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(long.class, Arrays.asList(long.class, float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(float.class, Arrays.asList(float.class, double.class));
        WIDENED_PRIMITIVE_MAP.put(double.class, Arrays.asList(double.class));

        WIDENED_PRIMITIVE_TYPE_DESCRIPTION_MAP = WIDENED_PRIMITIVE_MAP.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> TypeDescription.ForLoadedType.of(entry.getKey()), 
                                entry -> entry.getValue().stream()
                                        .map( e -> TypeDescription.ForLoadedType.of(e) )
                                        .collect( Collectors.toList())
                        )
                );
    }


    /**
     * Loads the class with the given name using the given class loader.
     * Supports primitive type names (e.g., {@code "int"}, {@code "boolean"}).
     *
     * @param className   the class name to load
     * @param classLoader the class loader to use
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    public static Class<?> forName(String className, ClassLoader classLoader) 
            throws ClassNotFoundException {
        return forName(className, false, classLoader);
    }

    /**
     * Loads the class with the given name, optionally initializing it.
     * Supports primitive type names.
     *
     * @param className   the class name to load
     * @param initialize  whether to initialize the class
     * @param classLoader the class loader to use
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    public static Class<?> forName(String className, boolean initialize, ClassLoader classLoader) 
            throws ClassNotFoundException {
        Assert.notNull(className, "'className' must not be null.");
        Class<?> clazz = resolvePrimitieType(className);
        if (clazz != null)
            return clazz;

        try {
            return Class.forName(className, initialize, classLoader);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    private static Class<?> resolvePrimitieType(String className) {
        return PRIMITIVE_TYPE_MAP.containsKey(className) ? PRIMITIVE_TYPE_MAP.get(className) : null;
    }


    /**
     * Abbreviates a fully-qualified class name by reducing each package segment to its first character.
     *
     * @param className the fully-qualified class name
     * @return the abbreviated class name
     */
    public static String abbreviateClassName(String className) {
        if (!StringUtils.hasText(className))
            return "";

        String[] items = className.split("\\"+PACKAGE_SEPARATOR, -1);
        if (items.length == 0)
            return className;

        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < items.length - 1; index++) {
            sb.append(items[index].charAt(0)).append(PACKAGE_SEPARATOR);
        }
        sb.append(items[items.length-1]);
        return sb.toString();
    }

    /**
     * Abbreviates a fully-qualified class name to the given target length.
     *
     * @param className    the fully-qualified class name
     * @param targetLength the target length for abbreviation
     * @return the abbreviated class name
     */
    public static String abbreviate(String className, int targetLength) {
        return new TargetLengthBasedClassNameAbbreviator(targetLength).abbreviate(className);
    }


    /**
     * Resolves the {@link java.lang.reflect.AccessibleObject} (Method or Constructor) for the given
     * type and ByteBuddy method description.
     *
     * @param type              the declaring class
     * @param methodDescription the ByteBuddy method description
     * @return the corresponding {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
     * @throws ClassNotFoundException  if a parameter type cannot be loaded
     * @throws NoSuchMethodException   if the method or constructor is not found
     * @throws SecurityException       if access is denied
     */
    public static AccessibleObject getAccessibleObject(Class<?> type, MethodDescription methodDescription) 
            throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Assert.notNull(type, "'type' must not be null.");
        Assert.notNull(methodDescription, "'methodDescription' must not be null.");

        ClassLoader classLoader = type.getClassLoader();
        String methodName = methodDescription.getName();

        List<Class<?>> parameterTypes = new ArrayList<>(methodDescription.getParameters().size());
        for (ParameterDescription paramDescription : methodDescription.getParameters()) {
            TypeDescription parameterType = paramDescription.getType().asErasure();
            parameterTypes.add( ClassUtils.forName(parameterType.getTypeName(), false, classLoader) );
        }

        Class<?>[] parameters = parameterTypes.toArray( new Class[] {});
        return methodDescription.isConstructor()
                ? type.getDeclaredConstructor(parameters)
                : type.getDeclaredMethod(methodName, parameters);
    }


    /**
     * Determines if {@code lhsType} is the same as or a supertype of {@code rhsType},
     * with support for primitive/wrapper boxing.
     *
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    public static boolean isAssignableFrom(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType == null || rhsType == null)
            return false;

        if (isAssignablePrimitiveFrom(lhsType, rhsType, true)) {
            return true;
        }

        return lhsType.isAssignableFrom(rhsType);
    }

    /**
     * Determines if {@code lhsType} is the same as or a supertype of {@code rhsType},
     * with support for primitive/wrapper boxing.
     *
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @param allowWrapperToPrimitive  assign wrapper type to corresponding promitive type
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    public static boolean isAssignableFrom(Class<?> lhsType, Class<?> rhsType, boolean allowWrapperToPrimitive) {
        if (lhsType == null || rhsType == null)
            return false;

        if (isAssignablePrimitiveFrom(lhsType, rhsType, allowWrapperToPrimitive)) {
            return true;
        }

        return lhsType.isAssignableFrom(rhsType);
    }

    /**
     * Determines if {@code lhsType} is the same as of {@code rhsType},
     * with support for primitive/wrapper boxing.
     * 
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @param allowWrapperToPrimitive  assign wrapper type to corresponding promitive type
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    private static boolean isAssignablePrimitiveFrom(Class<?> lhsType, Class<?> rhsType, boolean allowWrapperToPrimitive) {
        if (lhsType.isPrimitive() && rhsType.isPrimitive()) {
            return WIDENED_PRIMITIVE_MAP.get(rhsType).contains(lhsType);
        } else if (allowWrapperToPrimitive && lhsType.isPrimitive()) {
            Class<?> lhsWrapperType = PRIMITIVE_WRAPPER_MAP.get(lhsType);
            return lhsWrapperType != null && lhsWrapperType.isAssignableFrom(rhsType);
        } else if (rhsType.isPrimitive()) {
            Class<?> rhsWrapperType = PRIMITIVE_WRAPPER_MAP.get(rhsType);
            return rhsWrapperType != null && lhsType.isAssignableFrom(rhsWrapperType);
        } else {
            return false;
        }
    }



    /**
     * Determines if {@code lhsType} is the same as or a supertype of {@code rhsType}
     * using ByteBuddy generic types, with support for primitive/wrapper boxing.
     *
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    public static boolean isAssignableFrom(TypeDescription lhsType, TypeDescription rhsType) {
        return isAssignableFrom(lhsType, rhsType, true);
    }

    /**
     * Determines if {@code lhsType} is the same as or a supertype of {@code rhsType}
     * using ByteBuddy generic types, with support for primitive/wrapper boxing.
     *
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @param allowWrapperToPrimitive  assign wrapper type to corresponding promitive type
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    public static boolean isAssignableFrom(TypeDescription lhsType, TypeDescription rhsType, boolean allowWrapperToPrimitive) {
        if (lhsType == null || rhsType == null)
            return false;

        // 1.void type
        if (lhsType.represents(void.class) || rhsType.represents(void.class))
            return true;

        // 2.primitive type
        if (isAssignablePrimitiveFrom(lhsType, rhsType, allowWrapperToPrimitive))
            return true;

        // 3.reference type
        return lhsType.isAssignableFrom(rhsType);
    }

    private static boolean isAssignablePrimitiveFrom(TypeDescription lhsType, TypeDescription rhsType,
            boolean allowWrapperToPrimitive) {
        if (lhsType.isPrimitive() && rhsType.isPrimitive()) {
            return WIDENED_PRIMITIVE_TYPE_DESCRIPTION_MAP.get(rhsType).contains(lhsType);
        } else if (allowWrapperToPrimitive && lhsType.isPrimitive()) {
            TypeDescription lhsWrapperType = PRIMITIVE_TYPE_DESCRIPTION_MAP.get(lhsType);
            return lhsWrapperType.isAssignableFrom(rhsType);
        } else if (rhsType.isPrimitive()) {
            TypeDescription rhsWrapperType = PRIMITIVE_TYPE_DESCRIPTION_MAP.get(rhsType);
            return lhsType.isAssignableFrom(rhsWrapperType);
        }

        return false;
    }


    /**
     * Determines if {@code lhsType} is the same as or a supertype of {@code rhsType}
     * using ByteBuddy generic types, with support for primitive/wrapper boxing.
     *
     * @param lhsType  the potential supertype
     * @param rhsType the potential subtype
     * @param allowWrapperToPrimitive  assign wrapper type to corresponding promitive type
     * @return {@code true} if {@code lhsType} is assignable from {@code rhsType}
     */
    public static boolean isAssignableFrom(Generic lhsType, Generic rhsType, boolean allowWrapperToPrimitive) {
        if (lhsType == null || rhsType == null)
            return false;

        // 1.void type
        if (lhsType.represents(void.class) || rhsType.represents(void.class))
            return true;

        // 2.primitive type
        if (isAssignablePrimitiveFrom(lhsType.asErasure(), rhsType.asErasure(), allowWrapperToPrimitive))
            return true;

        // 3.reference type
        return lhsType.accept(TypeDescription.Generic.Visitor.Assigner.INSTANCE).isAssignableFrom(rhsType);
    }


    /**
     * Determines if {@code lhsType} equals {@code rhsType}, with support for primitive/wrapper boxing.
     *
     * @param lhsType  the first type
     * @param rhsType the second type
     * @return {@code true} if the types are equal (considering boxing)
     */
    public static boolean equals(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType == null || rhsType == null)
            return false;

        if (isSamePrimitive(lhsType, rhsType))
            return true;

        return lhsType.equals(rhsType);
    }

    private static boolean isSamePrimitive(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isPrimitive() && rhsType.isPrimitive()) {
            return lhsType.equals(rhsType);
        } else if (lhsType.isPrimitive()) {
            Class<?> lhsWrapperType = PRIMITIVE_WRAPPER_MAP.get(lhsType);
            return lhsWrapperType != null && lhsWrapperType.equals(rhsType);
        } else if (rhsType.isPrimitive()) {
            Class<?> rhsWrapperType = PRIMITIVE_WRAPPER_MAP.get(rhsType);
            return rhsWrapperType != null && lhsType.equals(rhsWrapperType);
        }

        return false;
    }

    /**
     * Determines if {@code targetType} equals {@code sourceType}, with support for primitive/wrapper boxing.
     *
     * @param lhsType  the first type
     * @param rhsType the second type
     * @return {@code true} if the types are equal (considering boxing)
     */
    public static boolean equals(TypeDescription lhsType, TypeDescription rhsType) {
        if (lhsType == null || rhsType == null)
            return false;

        if (isSamePrimitive(lhsType, rhsType))
            return true;

        return lhsType.equals(rhsType);
    }

    private static boolean isSamePrimitive(TypeDescription lhsType, TypeDescription rhsType) {
        if (lhsType.isPrimitive() && rhsType.isPrimitive()) {
            return lhsType.equals(rhsType);
        } else if (lhsType.isPrimitive()) {
            TypeDescription lhsWrapperType = PRIMITIVE_TYPE_DESCRIPTION_MAP.get(lhsType);
            return lhsWrapperType.asBoxed().equals(rhsType);
        } else if (rhsType.isPrimitive()) {
            TypeDescription rhsWrapperType = PRIMITIVE_TYPE_DESCRIPTION_MAP.get(rhsType);
            return lhsType.equals(rhsWrapperType);
        }

        return false;
    }

    public static boolean equals(Generic lhsType, Generic rhsType) {
        if (lhsType == null || rhsType == null)
            return false;

        if (isSamePrimitive(lhsType.asErasure(), rhsType.asErasure()))
            return true;

        return lhsType.equals(rhsType);
    }


    /**
     * Determines if {@code calleeType} is visible to {@code callerType} across class loaders.
     * A type is visible if it is public, or if it is protected and in the same package.
     *
     * @param calleeType the type to check visibility of
     * @param callerType the type that needs to access the callee
     * @return {@code true} if {@code calleeType} is visible to {@code callerType}
     */
    public static boolean isVisibleTo(TypeDescription calleeType, TypeDescription callerType) {
        if (calleeType.isPrimitive())
            return true;

        // public or same package protected type
        return calleeType.isArray()
                ? isVisibleTo(calleeType.getComponentType(), callerType)
                : calleeType.isPublic() || (calleeType.isProtected() && calleeType.isSamePackage(callerType));
    }


    /**
     * Converts a fully-qualified class name to a resource path (replacing {@code '.'} with {@code '/'}).
     *
     * @param className the class name to convert
     * @return the resource path
     */
    public static String convertClassToResource(String className) {
        return convertClassToResource(className, false);
    }

    /**
     * Converts a fully-qualified class name to a resource path, optionally appending {@code ".class"}.
     *
     * @param className          the class name to convert
     * @param appendClassFileExt whether to append the {@code ".class"} extension
     * @return the resource path
     */
    public static String convertClassToResource(String className, boolean appendClassFileExt) {
        if (!StringUtils.hasText(className))
            return className;

        return className.replace(PACKAGE_SEPARATOR, RESOURCE_SPERATOR) 
                + (appendClassFileExt ? CLASS_FILE_EXTENSION : "");
    }
}