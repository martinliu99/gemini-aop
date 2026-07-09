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

import static net.bytebuddy.matcher.ElementMatchers.hasDescriptor;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.aop.AdviceKind;
import io.gemini.aop.AdviceKind.AspectJAdviceKind;
import io.gemini.aop.AdviceKind.ByteBuddyAdviceKind;
import io.gemini.aop.AdviceKind.PojoAdviceKind;
import io.gemini.aop.matcher.ExprPointcut;
import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint;
import io.gemini.aspectj.weaver.PointcutParameter;
import io.gemini.aspectj.weaver.PointcutParameter.NamedPointcutParameter;
import io.gemini.aspectj.weaver.PointcutParameter.ParamCategory;
import io.gemini.core.config.ConfigView;
import io.gemini.core.util.MethodUtils;
import io.gemini.core.util.Pair;
import io.gemini.core.util.SignatureParser;
import io.gemini.core.util.SignatureParser.MethodSignature;
import io.gemini.core.util.SignatureParser.TypeSignature;
import io.gemini.core.util.StringUtils;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.pool.TypePool;

/**
 * Describes the metadata of an advice method or class parsed from an aspect application.
 * <p>
 * Three sub-interfaces cover the three advice styles:
 * <ul>
 *   <li>{@link PojoAdviceSpec} – POJO before/after/around advice</li>
 *   <li>{@link AspectJAdviceSpec} – AspectJ annotation-based advice with pointcut parameter binding</li>
 *   <li>{@link ByteBuddyAdviceSpec} – raw ByteBuddy {@code @Advice} class</li>
 * </ul>
 * </p>
 *
 * @author   martin.liu
 */
public interface AdviceSpec extends AnnotationFinder {

    /**
     * Returns the advice kind (POJO, AspectJ, or ByteBuddy).
     *
     * @return the {@link AdviceKind}
     */
    AdviceKind getAdviceKind();

    /**
     * Returns the type name that declares the advice method or class.
     *
     * @return the declaring type name
     */
    String getDeclaringTypeName();

    /**
     * Returns the fully-qualified class name of the advice class.
     *
     * @return the advice class name
     */
    String getAdviceClassName();

    /** 
     * {@inheritDoc}
     */
    @Override
    AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass);


    /**
     * Loads {@link AdviceSpec} per the given target {@link TypePool}.
     */
    interface Loadable<T extends AdviceSpec> {

        /**
         * Loads {@link AdviceSpec} per the given target {@link TypePool}.
         * 
         * @param targetTypePool the target {@link TypePool}
         * @return the instance of {@link AdviceSpec}
         */
        T load(TypePool targetTypePool);

    }


    /**
     * Abstract base providing common advice spec properties: advice kind, declaring type,
     * advice class name, advice method, and parameterized return/throw types.
     */
    abstract class AbstractBase implements AdviceSpec {

        protected static final Logger LOGGER = LoggerFactory.getLogger(AdviceSpec.class);

        protected static final Pair<String, String> UNDEFIEND_NAME_PAIR = new Pair<String, String>(null, null);
        protected static final Pair<TypeDescription, TypeDescription> UNDEFIEND_TYPE_PAIR = new Pair<TypeDescription, TypeDescription>(null, null);


        private AdviceKind adviceKind;

        private TypeDescription declaringType;

        private String adviceClassName;


        public AbstractBase(AdviceKind adviceKind, TypeDescription declaringType, String adviceClassName) {
            this.adviceKind = adviceKind;

            this.declaringType = declaringType;
            this.adviceClassName = adviceClassName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AdviceKind getAdviceKind() {
            return adviceKind;
        }

        /**
         * Sets the advice kind. Called by subclasses during initialization.
         *
         * @param adviceKind the advice kind to set
         */
        protected void setAdviceKind(AdviceKind adviceKind) {
            this.adviceKind = adviceKind;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDeclaringTypeName() {
            return getDeclaringType().getTypeName();
        }

        protected TypeDescription getDeclaringType() {
            return declaringType;
        }

        protected void setDeclaringType(TypeDescription declaringType) {
            this.declaringType = declaringType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAdviceClassName() {
            return adviceClassName;
        }

        /**
         * Sets the advice class name. Called by subclasses during initialization.
         *
         * @param adviceClassName the fully-qualified advice class name
         */
        protected void setAdviceClassName(String adviceClassName) {
            this.adviceClassName = adviceClassName;
        }


        protected MethodSignature createMethodSignature(MethodDescription methodDescription) {
            String methodSignature = methodDescription.getGenericSignature();
            if (methodSignature == null)
                methodSignature = methodDescription.getDescriptor();

            return SignatureParser.parseMethodSignature(methodSignature);
        }

        protected Pair<String, String> resolveJoinpointParamTypeArguments(String declaringTypeName, 
                MethodDescription methodDescription, MethodSignature methodSignature) {
            List<? extends TypeSignature> parameters = methodSignature.getParameters();
            if (parameters.size() == 0)
                return null;

            TypeSignature joinpointType = parameters.get(0);
            if (!Joinpoint.MutableJoinpoint.class.getName().equals(joinpointType.getTypeName())
                    || TypeDefinition.Sort.PARAMETERIZED != joinpointType.getSort() ) {
                return null;
            }

            List<? extends TypeSignature> typeArguments = joinpointType.getTypeArguments();

            TypeSignature returnType = typeArguments.get(0);
            if (TypeDefinition.Sort.NON_GENERIC != returnType.getSort() && TypeDefinition.Sort.PARAMETERIZED != returnType.getSort()) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ignored advice method with Generic or WildcardType ParameterizedReturning of MutableJoinpoint. \n"
                            + "  DeclaringType: {} \n"
                            + "  AdviceMethod: {} \n"
                            + "    ParameterizedReturning: {} \n",
                            declaringTypeName,
                            toString(declaringTypeName, methodDescription, methodSignature),
                            returnType.getTypeName()
                    );

                return null;
            }

            TypeSignature throwType = typeArguments.get(1);
            if (TypeDefinition.Sort.NON_GENERIC != throwType.getSort() && TypeDefinition.Sort.PARAMETERIZED != throwType.getSort()) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Ignored advice method with Generic or WildcardType ParameterizedThrowing of MutableJoinpoint. \n"
                            + "  DeclaringType: {} \n"
                            + "  AdviceMethod: {} \n"
                            + "    ParameterizedThrowing: {} \n",
                            declaringTypeName,
                            toString(declaringTypeName, methodDescription, methodSignature),
                            throwType.getTypeName()
                    );

                return null;
            }

            return new Pair<>(returnType.getTypeName(), throwType.getTypeName());
        }

        protected MethodDescription loadAdviceMethod(TypePool targetTypePool, MethodDescription adviceMethod) {
            TypeDescription declaringType = targetTypePool.describe(
                    adviceMethod.getDeclaringType().getTypeName() ).resolve();

            return declaringType.getDeclaredMethods().filter(
                    named( adviceMethod.getName() )
                    .and( hasDescriptor( adviceMethod.getDescriptor() ) )
            )
            .getOnly();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
            return this.declaringType != null 
                    ? this.declaringType.getDeclaredAnnotations().ofType(annotationClass)
                    : null;
        }

        protected String toString(String declaringTypeName, MethodDescription methodDescription, MethodSignature methodSignature) {
            StringBuilder stringBuilder = new StringBuilder();
            int modifiers = methodDescription.getModifiers();
            stringBuilder.append(Modifier.toString(modifiers)).append(' ');

            if (methodDescription.isMethod()) {
                stringBuilder.append(methodSignature.getReturnType().getTypeName()).append(' ');
                stringBuilder.append(declaringTypeName).append('.');
            }
            stringBuilder.append(methodDescription.getName()).append('(');
            boolean first = true;
            for (TypeSignature typeSignature : methodSignature.getParameters()) {
                if (!first) {
                    stringBuilder.append(',');
                } else {
                    first = false;
                }
                stringBuilder.append(typeSignature.getTypeName());
            }
            stringBuilder.append(')');

            return stringBuilder.toString();
        }
    }


    /** 
     * POJO-style advice spec for before/after/around advice implementations. 
     */
    interface PojoAdviceSpec extends AdviceSpec {

        /**
         * Returns the POJO advice kind (BEFORE, AFTER, BEFORE_AFTER, or AROUND).
         *
         * @return the {@link PojoAdviceKind}
         */
        @Override
        PojoAdviceKind getAdviceKind();


        /**
         * Subtype of {@link PojoAdviceSpec} holds metadata used to load {@link PojoAdviceSpec.Loaded}
         * instance per target {@link TypePool}.
         */
        interface Unloaded extends PojoAdviceSpec, Loadable<PojoAdviceSpec.Loaded> {

            /**
             * The default implementation of {@link PojoAdviceSpec.Unloaded}.
             */
            class Default extends AbstractBase implements Unloaded {

                private final MethodDescription adviceMethod;

                private Pair<String, String> joinpointTypeArgumentNames = UNDEFIEND_NAME_PAIR;


                /**
                 * Creates a POJO advice spec.
                 *
                 * @param adviceKind                 the POJO advice kind
                 * @param declaringType          the declaring type name
                 * @param adviceClassName            the fully-qualified advice class name
                 * @param adviceMethod               the advice method (before/after/invoke)
                 */
                public Default(PojoAdviceKind adviceKind, TypeDescription declaringType, 
                        String adviceClassName, MethodDescription adviceMethod) {
                    super(adviceKind, declaringType, adviceClassName);

                    this.adviceMethod = adviceMethod;
                }


                /** 
                 * {@inheritDoc}
                 */
                @Override
                public PojoAdviceKind getAdviceKind() {
                    return (PojoAdviceKind) super.getAdviceKind();
                }

                public MethodDescription getAdviceMethod() {
                    return adviceMethod;
                }

                public Pair<String, String> getJoinpointTypeArgumentNames() {
                    if (this.joinpointTypeArgumentNames != null &&
                            this.joinpointTypeArgumentNames.equals(UNDEFIEND_NAME_PAIR))
                        this.joinpointTypeArgumentNames = resolveJoinpointParamTypeArguments(
                                getDeclaringTypeName(), getAdviceMethod(), createMethodSignature(adviceMethod));

                    return this.joinpointTypeArgumentNames;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public PojoAdviceSpec.Loaded load(TypePool targetTypePool) {
                    return new PojoAdviceSpec.Loaded.Default(targetTypePool, this);
                }
            }
        }


        /**
         * Subtype of {@link PojoAdviceSpec} parses metadata, e.g., type arguments of joinpoint parameter
         * which should be resolved per target {@link TypePool}.
         */
        interface Loaded extends PojoAdviceSpec {

            /**
             * Returns the type that declares the advice method or class.
             *
             * @return the declaring {@link TypeDescription}
             */
            TypeDescription getDeclaringType();

            /**
             * Returns the advice method description.
             *
             * @return the advice {@link MethodDescription}.
             */
            MethodDescription getAdviceMethod();

            /**
             * Returns type arguments of the joinpoint parameter.
             * 
             * @return type argument representing returning and throwing types
             */
            Pair<TypeDescription, TypeDescription> getJoinpointTypeArgumentTypes();


            /**
             * The default implementation of {@link PojoAdviceSpec.Loaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements PojoAdviceSpec.Loaded {

                private final TypePool targetTypePool;
                private final PojoAdviceSpec.Unloaded.Default adviceSpec;

                private MethodDescription adviceMethod;
                private Pair<TypeDescription, TypeDescription> joinpointTypeArgumentTypes = UNDEFIEND_TYPE_PAIR;


                public Default(TypePool targetTypePool, PojoAdviceSpec.Unloaded.Default adviceSpec) {
                    super(adviceSpec.getAdviceKind(), null, adviceSpec.getAdviceClassName());

                    this.targetTypePool = targetTypePool;
                    this.adviceSpec = adviceSpec;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public PojoAdviceKind getAdviceKind() {
                    return (PojoAdviceKind) super.getAdviceKind();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public TypeDescription getDeclaringType() {
                    if (super.getDeclaringType() == null)
                        this.setDeclaringType(
                                targetTypePool.describe(adviceSpec.getDeclaringTypeName()).resolve() );
                    return super.getDeclaringType();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public MethodDescription getAdviceMethod() {
                    if (this.adviceMethod == null) 
                        this.adviceMethod = loadAdviceMethod(targetTypePool, adviceSpec.getAdviceMethod());

                    return this.adviceMethod;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public Pair<TypeDescription, TypeDescription> getJoinpointTypeArgumentTypes() {
                    if (this.joinpointTypeArgumentTypes != null 
                            && joinpointTypeArgumentTypes.equals(UNDEFIEND_TYPE_PAIR)) {
                        Pair<String, String> typeArgumentNames = adviceSpec.getJoinpointTypeArgumentNames();
                        this.joinpointTypeArgumentTypes = typeArgumentNames == null 
                                ? null
                                : new Pair<>(
                                        targetTypePool.describe(typeArgumentNames.getLeft()).resolve(),
                                        targetTypePool.describe(typeArgumentNames.getRight()).resolve()
                                );
                    }
                    return joinpointTypeArgumentTypes;
                }
            }
        }
    }


    /**
     * AspectJ annotation-based advice spec.
     */
    interface AspectJAdviceSpec extends AdviceSpec {

        /** 
         * Returns the AspectJ advice kind (@Before, @After, @AfterReturning, @AfterThrowing, @Around). 
         */
        @Override
        AspectJAdviceKind getAdviceKind();

        /** 
         * Returns the AspectJ advice annotation (e.g., {@code @Before}, {@code @Around}). 
         */
        AnnotationDescription getAdviceAnnotation();


        /**
         * Subtype of {@link AspectJAdviceSpec} holds metadata used to load {@link AspectJAdviceSpec.Loaded}
         * instance per target {@link TypePool}.
         */
        interface Unloaded extends AspectJAdviceSpec, Loadable<AspectJAdviceSpec.Loaded> {

            /**
             * The default implementation of {@link AspectJAdviceSpec.Unloaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements AspectJAdviceSpec.Unloaded {

                private static final List<String> ACCESSIBLE_OBJECTS = Arrays.asList( 
                        AccessibleObject.class.getName(),
                        Executable.class.getName(),
                        Constructor.class.getName(),
                        Method.class.getName()
                );


                private final AnnotationDescription adviceAnnotation;

                private final MethodDescription adviceMethod;
                private MethodSignature adviceMethodSignature;

                private Pair<String, String> joinpointTypeArgumentNames = UNDEFIEND_NAME_PAIR;

                private Map<String, String> parameterMap;
                private Map<String, ParamCategory> resolvedParameters;


                public Default(TypeDescription declaringType, MethodDescription adviceMethod,
                        AnnotationDescription adviceAnnotation) throws IllegalSpecException {
                    super(null, declaringType, null);

                    this.adviceMethod = adviceMethod;
                    this.adviceAnnotation = adviceAnnotation;

                    String adviceKindValue = adviceAnnotation.getAnnotationType().getSimpleName().toUpperCase(Locale.ENGLISH);
                    setAdviceKind( AspectJAdviceKind.parse(adviceKindValue) );

                    setAdviceClassName( generateAdviceClassName(adviceKindValue, adviceMethod) );


                    String argNamesStr = adviceAnnotation.getValue("argNames").resolve(String.class).trim();
                    String returningParameterName = getAdviceKind().isAfterReturning()
                            ? adviceAnnotation.getValue("returning").resolve(String.class).trim() : null;
                    String throwingParameterName = getAdviceKind().isAfterThrowing()
                            ? adviceAnnotation.getValue("throwing").resolve(String.class).trim() : null;

                    initialize(declaringType, adviceMethod, getAdviceKind(), getAdviceMethodSignature(), 
                            argNamesStr, returningParameterName, throwingParameterName);
                }


                public Default(TypeDescription declaringType, MethodDescription adviceMethod,
                        ConfigView configView, String configKeyPrefix) throws IllegalSpecException {
                    super(null, declaringType, null);

                    this.adviceMethod = adviceMethod;
                    this.adviceAnnotation = null;

                    String adviceKindKey = configKeyPrefix + "adviceCategory";
                    String adviceKindValue = configView.getAsString(adviceKindKey, "").toUpperCase(Locale.ENGLISH);
                    try {
                        setAdviceKind( AspectJAdviceKind.parse(adviceKindValue) );
                    } catch (Exception e) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored AspectJ advice method with illegal 'adviceKind' configuration property. \n"
                                    + "  ConfigKeyPrefix: {} \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    {}: {} \n"
                                    + "  Error reason: {} \n", 
                                    configKeyPrefix,
                                    declaringType, 
                                    toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                    adviceKindKey, adviceKindValue,
                                    e.getMessage()
                            );

                        throw new IllegalSpecException();
                    }

                    setAdviceClassName( generateAdviceClassName(adviceKindValue, adviceMethod) );


                    String argNamesStr = configView.getAsString(configKeyPrefix + "argNames", "");
                    String returningParameterName = getReturingParameterName(configView, configKeyPrefix);
                    String throwingParameterName = getThrowingParameterName(configView, configKeyPrefix);

                    initialize(declaringType, adviceMethod, getAdviceKind(), getAdviceMethodSignature(), 
                            argNamesStr, returningParameterName, throwingParameterName);
                }

                private String getReturingParameterName(ConfigView configView, String configKeyPrefix) {
                    String returningKey = configKeyPrefix + "returning";
                    String returningParameterName = configView.getAsString(returningKey, "");
                    if (getAdviceKind().isAfterReturning()) 
                        return returningParameterName;

                    if (LOGGER.isInfoEnabled() && StringUtils.hasText(returningParameterName))
                        LOGGER.info("Ignored meaningless 'returning' property of non AspectJ @AfterReturning advice method. \n"
                                + "  ConfigKeyPrefix: {} \n"
                                + "  DeclaringType: {} \n"
                                + "    {}: {} \n",
                                configKeyPrefix,
                                getDeclaringTypeName(), 
                                returningKey, returningParameterName
                        );
                    return null;
                }

                private String getThrowingParameterName(ConfigView configView, String configKeyPrefix) {
                    String throwingKey = configKeyPrefix + "throwing";
                    String throwingParameterName = configView.getAsString(throwingKey, "");
                    if (getAdviceKind().isAfterThrowing()) 
                        return throwingParameterName;

                    if (LOGGER.isInfoEnabled() && StringUtils.hasText(throwingParameterName))
                        LOGGER.info("Ignored meaningless 'throwing' property of non AspectJ @AfterThrowing advice method. \n"
                                + "  ConfigKeyPrefix: {} \n"
                                + "  DeclaringType: {} \n"
                                + "    {}: {} \n",
                                configKeyPrefix,
                                getDeclaringTypeName(), 
                                throwingKey, throwingParameterName
                        );
                    return null;
                }

                private String generateAdviceClassName(String adviceKind, MethodDescription adviceMethod) {
                    // define advice class name
                    return getDeclaringTypeName() + "_$$" + adviceMethod.getName() + "_" + adviceKind + "$$";
                }

                private void initialize(TypeDescription declaringType, 
                        MethodDescription adviceMethod, AspectJAdviceKind adviceKind, 
                        MethodSignature adviceMethodSignature, String argNamesStr, 
                        String returningParameterName, String throwingParameterName) {
                    // 1.resolve parameter name/type pair
                    List<String> parameterNames = resolveParameterNames(declaringType, adviceMethod, argNamesStr);
                    this.parameterMap = createParameterTypeMap(parameterNames, adviceMethodSignature.getParameterTypes());


                    // 2.bind AspectJ method parameters
                    this.resolvedParameters = new LinkedHashMap<>(3);

                    resolveJoinpointParamBinding(resolvedParameters);

                    if (adviceKind.isAfterReturning())
                        resolveAdviceReturningParamBinding(resolvedParameters, returningParameterName);

                    if (adviceKind.isAfterThrowing())
                        resolveAdviceThrowingParamBinding(resolvedParameters, throwingParameterName);
                }

                private List<String> resolveParameterNames(TypeDescription declaringType, 
                        MethodDescription adviceMethod, String argNamesStr) {
                    ParameterList<ParameterDescription.InDefinedShape> parameters = adviceMethod.asDefined().getParameters();
                    if (parameters.size() == 0)
                        return new ArrayList<>();

                    ParameterDescription.InDefinedShape index0Param = parameters.get(0);
                    List<String> parameterNames = new ArrayList<>(parameters.size());

                    // 1.fetch 'argNames' value in annotation
                    StringTokenizer st = new StringTokenizer(argNamesStr, ",");
                    List<String> argNames = new ArrayList<>(st.countTokens());
                    while (st.hasMoreTokens())
                        argNames.add(st.nextToken().trim());

                    if (argNames.size() > 0) {
                        if (argNames.size() != parameters.size() && argNames.size() != parameters.size() - 1) {
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn("Ignored AspectJ advice method with parameters is inconsistent with 'argNames' annotation attribute. \n"
                                        + "  DeclaringType: {} \n"
                                        + "  AdviceMethod: {} \n"
                                        + "    ArgNames: {} \n", 
                                        declaringType,
                                        toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                        argNamesStr
                                );

                            throw new IllegalSpecException();
                        }

                        // first parameter should be joinpoint
                        if (argNames.size() == parameters.size() - 1) {
                            parameterNames.add(index0Param.getName());
                        }
                        parameterNames.addAll(argNames);

                        return parameterNames;
                    }

                    // 2.parse parameter names in MethodParameters section
                    // validate parameter name for index0 parameter
//                    if (index0Param.getName().equals(index0Param.getActualName()) == false) {
//                        if (LOGGER.isWarnEnabled())
//                            LOGGER.warn("Ignored AspectJ advice method without parameter reflection support and 'argNames' annotation attribute. \n"
//                                    + "  DeclaringType: {} \n"
//                                    + "  AdviceMethod: {} \n"
//                                    + "    BinaryMethodName: {} \n"
//                                    + "    ActualMethodName: {} \n",
//                                    declaringType.getTypeName(),
//                                    MethodUtils.getMethodSignature(adviceMethod), 
//                                    index0Param.getName(), 
//                                    index0Param.getActualName()
//                            );
    //
//                        throw new IllegalSpecException();
//                    }

                    return parameters.stream()
                            .map( p -> p.getName() )
                            .collect( Collectors.toList() );
                }


                private Map<String, String> createParameterTypeMap(List<String> parameterNames, List<String> parameterTypes) {
                    Map<String, String> parameterTypeMap = new LinkedHashMap<>(parameterNames.size());

                    // access by parameter index other than parameter name which might NOT contain in bytecode file
                    for (int index = 0; index < parameterNames.size(); index++) {
                        parameterTypeMap.put(parameterNames.get(index), parameterTypes.get(index));
                    }

                    return parameterTypeMap;
                }

                private void resolveJoinpointParamBinding(Map<String, ParamCategory> resolvedParameters) {
                    if (parameterMap.size() == 0) 
                        return;

                    // bind first parameter
                    ParameterDescription.InDefinedShape parameterDescription = getAdviceMethod().asDefined().getParameters().get(0);
                    String parameterName = parameterDescription.getName();
                    String parameterTypeName = parameterMap.get(parameterName);

                    if (Joinpoint.class.getName().equals(parameterTypeName))
                        resolvedParameters.put(parameterName, ParamCategory.JOINPOINT_PARAM);
                    else if (Joinpoint.MutableJoinpoint.class.getName().equals(parameterTypeName))
                        resolvedParameters.put(parameterName, ParamCategory.MUTABLE_JOINPOINT_PARAM);
                    else if (ACCESSIBLE_OBJECTS.contains(parameterTypeName))
                        resolvedParameters.put(parameterName, ParamCategory.MUTABLE_JOINPOINT_PARAM);
                }

                private void resolveAdviceReturningParamBinding(Map<String, ParamCategory> resolvedParameters, String returningParameterName) {
                    // resolve returning parameters
                    if (!StringUtils.hasText(returningParameterName)) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored AspectJ @AfterReturning advice method without 'returning' annotation attribute. \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    'returning': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                    returningParameterName
                            );

                        throw new IllegalSpecException();
                    }

                    if (!parameterMap.containsKey(returningParameterName)) { 
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored AspectJ @AfterReturning advice method with 'returning' annotation attribute referring to nonexistent parameter. \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    'returning': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                    returningParameterName
                            );

                        throw new IllegalSpecException();
                    }

                    resolvedParameters.put(returningParameterName, ParamCategory.RETURNING_ANNOTATION);
                }

                private void resolveAdviceThrowingParamBinding(Map<String, ParamCategory> resolvedParameters, String throwingParameterName) {
                    // resolve throwing parameters
                    if (!StringUtils.hasText(throwingParameterName)) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored AspectJ @AfterThrowing advice method without 'throwing' annotation attribute. \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    'throwing': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                    throwingParameterName
                            );

                        throw new IllegalSpecException();
                    }

                    if (!parameterMap.containsKey(throwingParameterName)) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored AspectJ @AfterThrowing advice method with 'throwing' annotation attribute referring to nonexistent parameter. \n"
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    'throwing': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod, getAdviceMethodSignature()),
                                    throwingParameterName
                            );

                        throw new IllegalSpecException();
                    }

                    resolvedParameters.put(throwingParameterName, ParamCategory.THROWING_ANNOTATION);
                }


                /**
                 * {@inheritDoc}
                 */
                @Override
                public AspectJAdviceKind getAdviceKind() {
                    return (AspectJAdviceKind) super.getAdviceKind();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
                    AnnotationDescription annotationDescription = this.adviceMethod.getDeclaredAnnotations().ofType(annotationClass);
                    return annotationDescription != null ? annotationDescription : super.getAnnotation(annotationClass);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationDescription getAdviceAnnotation() {
                    return adviceAnnotation;
                }

                public MethodDescription getAdviceMethod() {
                    return adviceMethod;
                }

                private MethodSignature getAdviceMethodSignature() {
                    if (this.adviceMethodSignature == null)
                        this.adviceMethodSignature = createMethodSignature(adviceMethod);

                    return this.adviceMethodSignature;
                }

                public Pair<String, String> getJoinpointTypeArgumentNames() {
                    if (this.joinpointTypeArgumentNames != null 
                            && this.joinpointTypeArgumentNames.equals(UNDEFIEND_NAME_PAIR))
                        this.joinpointTypeArgumentNames = resolveJoinpointParamTypeArguments(
                                getDeclaringTypeName(), getAdviceMethod(), getAdviceMethodSignature());

                    return this.joinpointTypeArgumentNames;
                }

                public Map<String, String> getParameterMap() {
                    return parameterMap;
                }

                public Map<String, ParamCategory> getResolvedParameters() {
                    return resolvedParameters;
                }

                public Collection<String> getPointcutParameters() {
                    List<String> parameters = new ArrayList<>(parameterMap.keySet());
                    parameters.removeAll(resolvedParameters.keySet());
                    return parameters;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public AspectJAdviceSpec.Loaded load(TypePool targetTypePool) {
                    return new AspectJAdviceSpec.Loaded.Default(targetTypePool, this);
                }
            }
        }


        /**
         * Subtype of {@link AspectJAdviceSpec} parses the pointcut parameter bindings, 
         * returning/throwing parameter types, and generates the unloaded advice class via
         * {@link AdviceClassGenerator}.
         */
        interface Loaded extends AspectJAdviceSpec, ExprPointcut.PointcutParameterMatcher {

            /**
             * Returns the type that declares the advice method or class.
             *
             * @return the declaring {@link TypeDescription}
             */
            TypeDescription getDeclaringType();

            /**
             * Returns the advice method description.
             *
             * @return the advice {@link MethodDescription}.
             */
            MethodDescription getAdviceMethod();

            /**
             * Returns type arguments of the joinpoint parameter.
             * 
             * @return type argument representing returning and throwing types
             */
            Pair<TypeDescription, TypeDescription> getJoinpointTypeArgumentTypes();

            /**
             * Returns the map of pointcut parameter names to their type names,
             * used for parameter binding in the generated advice class.
             *
             * @return map of parameter name to generic type
             */
            Map<String, String> getPointcutParameters();

            /**
             * Returns the resolved named pointcut parameters with their binding categories
             * (e.g., args, this, target, returning, throwing).
             *
             * @return map of parameter name to {@link NamedPointcutParameter}
             */
            Map<String, NamedPointcutParameter> getNamedPointcutParameters();

            /**
             * Returns {@code true} if the target method returns {@code void},
             * which affects how the returning parameter is handled in the generated advice.
             *
             * @return {@code true} if the target method is void-returning
             */
            boolean isVoidReturning();

            /**
             * Returns the dynamically generated, unloaded {@link Advice} class that wraps
             * the AspectJ advice method for use with the Gemini AOP framework dispatch mechanism.
             *
             * @return the unloaded advice class
             */
            DynamicType.Unloaded<? extends Advice> getUnloadedAdviceClass();


            /**
             * The default implementation of {@link AspectJAdviceSpec.Loaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements AspectJAdviceSpec.Loaded {

                private final TypePool targetTypePool;
                private final AspectJAdviceSpec.Unloaded.Default adviceSpec;

                private MethodDescription adviceMethod;
                private Pair<TypeDescription, TypeDescription> joinpointTypeArgumentTypes = UNDEFIEND_TYPE_PAIR;

                private Map<String, NamedPointcutParameter> namedPointcutParameters;
                private boolean voidReturning = false;
                private DynamicType.Unloaded<? extends Advice> unloadedAdviceClass;


                public Default(TypePool targetTypePool, AspectJAdviceSpec.Unloaded.Default adviceSpec) throws IllegalSpecException {
                    super(adviceSpec.getAdviceKind(), null, adviceSpec.getAdviceClassName());

                    this.targetTypePool = targetTypePool;
                    this.adviceSpec = adviceSpec;

                    this.namedPointcutParameters = new LinkedHashMap<>();
                }


                /**
                 * {@inheritDoc}
                 */
                @Override
                public AspectJAdviceKind getAdviceKind() {
                    return (AspectJAdviceKind) super.getAdviceKind();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public TypeDescription getDeclaringType() {
                    if (super.getDeclaringType() == null)
                        this.setDeclaringType(
                                targetTypePool.describe(adviceSpec.getDeclaringTypeName()).resolve() );
                    return super.getDeclaringType();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
                    AnnotationDescription annotationDescription = this.adviceMethod.getDeclaredAnnotations().ofType(annotationClass);
                    return annotationDescription != null ? annotationDescription : super.getAnnotation(annotationClass);
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationDescription getAdviceAnnotation() {
                    return adviceSpec.getAdviceAnnotation();
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public MethodDescription getAdviceMethod() {
                    if (this.adviceMethod == null)
                        this.adviceMethod = loadAdviceMethod(targetTypePool, adviceSpec.getAdviceMethod());

                    return this.adviceMethod;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public Pair<TypeDescription, TypeDescription> getJoinpointTypeArgumentTypes() {
                    if (this.joinpointTypeArgumentTypes != null 
                            && joinpointTypeArgumentTypes.equals(UNDEFIEND_TYPE_PAIR)) {
                        Pair<String, String> typeArgumentNames = adviceSpec.getJoinpointTypeArgumentNames();
                        joinpointTypeArgumentTypes = typeArgumentNames == null 
                                ? null
                                : new Pair<>(
                                        targetTypePool.describe(typeArgumentNames.getLeft()).resolve(),
                                        targetTypePool.describe(typeArgumentNames.getRight()).resolve()
                                );
                    }
                    return joinpointTypeArgumentTypes;
                }


                /** 
                 * {@inheritDoc}
                 */
                @Override
                public Map<String, String> getPointcutParameters() {
                    Collection<String> pointcutParameterNames = adviceSpec.getPointcutParameters();

                    Map<String, String> parameterMap = adviceSpec.getParameterMap();
                    Map<String, String> pointcutParameters = new LinkedHashMap<>(pointcutParameterNames.size());
                    for (String parameterName : pointcutParameterNames)
                        pointcutParameters.put(parameterName, parameterMap.get(parameterName));
                    return pointcutParameters;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean match(MethodDescription targetMethod, List<PointcutParameter> pointcutParameters) {
                    // 1.match parameter count
                    Collection<String> unresoledPointcutParameters = adviceSpec.getPointcutParameters();
                    if (pointcutParameters == null || pointcutParameters.size() != unresoledPointcutParameters.size()) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Ignored advice method with advice parameter count is different to target method's resolved parameters. \n" 
                                    + "  DeclaringType: {} \n"
                                    + "  AdviceMethod: {} \n"
                                    + "    AdviceParameters: {} \n"
                                    + "  TargetMethod: {} \n"
                                    + "    ParameterIndex: {} \n",
                                    adviceSpec.getDeclaringTypeName(),
                                    toString(adviceSpec.getDeclaringTypeName(), getAdviceMethod(), adviceSpec.getAdviceMethodSignature()), 
                                    unresoledPointcutParameters,
                                    MethodUtils.getMethodSignature(targetMethod),
                                    pointcutParameters == null ? null : pointcutParameters.stream()
                                            .map( p -> p.getArgsIndex() + ", " )
                                            .collect( Collectors.toList() )
                            );

                        return false;
                    }


                    // 2.collect resolved pointcut parameter
                    Map<String, NamedPointcutParameter> namedPointcutParameters = new LinkedHashMap<>(adviceSpec.getParameterMap().size());
                    for (Entry<String, ParamCategory> entry : adviceSpec.getResolvedParameters().entrySet()) {
                        String paramName = entry.getKey();
                        ParamCategory paramCategory = entry.getValue();
                        TypeDescription parameType = targetTypePool.describe(adviceSpec.getParameterMap().get(paramName)).resolve();
                        PointcutParameter.NamedPointcutParameter pointcutParameter = new PointcutParameter.Default(paramName, parameType, paramCategory); 

                        namedPointcutParameters.put(paramName, pointcutParameter);
                    }

                    int i = 0;
                    for (String paramName : unresoledPointcutParameters) {
                        PointcutParameter pointcutParameterBinding = pointcutParameters.get(i++);
                        if (pointcutParameterBinding == null) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Ignored advice method with advice parameters do not contain target method's resolved parameter '{}'. \n" 
                                        + "  DeclaringType: {} \n"
                                        + "  AdviceMethod: {} \n"
                                        + "    AdviceParameters: {} \n"
                                        + "  TargetMethod: {} \n"
                                        + "    ParameterIndex: {} \n",
                                        paramName, 
                                        adviceSpec.getDeclaringTypeName(),
                                        toString(adviceSpec.getDeclaringTypeName(), getAdviceMethod(), adviceSpec.getAdviceMethodSignature()), 
                                        unresoledPointcutParameters,
                                        MethodUtils.getMethodSignature(targetMethod),
                                        pointcutParameters == null ? null : pointcutParameters.stream()
                                                .map( p -> p.getArgsIndex() + ", " )
                                                .collect( Collectors.toList() )
                                );
                            }

                            return false;
                        }

                        namedPointcutParameters.put(paramName, new PointcutParameter.Default(
                                paramName, 
                                targetTypePool.describe(adviceSpec.getParameterMap().get(paramName)).resolve(), 
                                pointcutParameterBinding)
                        );
                    }
                    this.namedPointcutParameters = namedPointcutParameters;

                    Generic returnType = targetMethod.getReturnType();
                    this.voidReturning = returnType.represents(void.class);

                    return true;
                }


                /** 
                 * {@inheritDoc}
                 */
                @Override
                public Map<String, NamedPointcutParameter> getNamedPointcutParameters() {
                    return namedPointcutParameters;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public boolean isVoidReturning() {
                    return voidReturning;
                }

                /** 
                 * {@inheritDoc}
                 */
                @Override
                public DynamicType.Unloaded<? extends Advice> getUnloadedAdviceClass() {
                    if (unloadedAdviceClass == null)
                        this.unloadedAdviceClass = AdviceClassGenerator.INSTANCE.make(this, true);

                    return unloadedAdviceClass;
                }
            }
        }
    }


    /** 
     * Raw ByteBuddy {@code @Advice} class spec, incuding enterAdviceMethod and exitAdviceMethod.
     */
    interface ByteBuddyAdviceSpec extends AdviceSpec {

        /**
         * Represent advice method annotated with {@link OnMethodEnter} or {@link OnMethodExit}.
         * 
         * @return the enter advice method instance
         */
        List<ByteBuddyAdviceMethodSpec> getAdviceMethodSpecs();


        class ByteBuddyAdviceMethodSpec {

            private final boolean enterMethod;
            private final MethodDescription adviceMethod;

            private final boolean voidReturn;
            private final boolean assignReturnedToThis;

            public ByteBuddyAdviceMethodSpec(boolean enterMethod, MethodDescription adviceMethod) {
                this.enterMethod = enterMethod;
                this.adviceMethod = adviceMethod;

                this.voidReturn = adviceMethod.getReturnType().represents(void.class);
                this.assignReturnedToThis = adviceMethod.getDeclaredAnnotations().isAnnotationPresent(
                        net.bytebuddy.asm.Advice.AssignReturned.ToThis.class);
            }

            public MethodDescription getAdviceMethod() {
                return adviceMethod;
            }

            public boolean isEnterMethod() {
                return enterMethod;
            }

            public boolean isVoidReturn() {
                return voidReturn;
            }

            public boolean isAssignReturnedToThis() {
                return assignReturnedToThis;
            }
        }


        /**
         * Default {@link ByteBuddyAdviceSpec} implementation for raw ByteBuddy {@code @Advice} classes.
         */
        class Default extends AbstractBase implements ByteBuddyAdviceSpec {

            private final List<ByteBuddyAdviceMethodSpec> adviceMethodSpecs;


            /**
             * Creates a ByteBuddy advice spec from the given advice class type.
             *
             * @param adviceKind    the ByteBuddy advice kind (OnMethodEnter, OnMethodExit, or both)
             * @param declaringType the ByteBuddy {@code @Advice} class type description
             */
            public Default(ByteBuddyAdviceKind adviceKind, TypeDescription declaringType,
                    MethodDescription enterAdviceMethod, MethodDescription exitAdviceMethod) {
                super(adviceKind, declaringType, declaringType.getTypeName());

                List<ByteBuddyAdviceMethodSpec> adviceMethodSpecs = new ArrayList<>(2);
                if (enterAdviceMethod != null)
                    adviceMethodSpecs.add( new ByteBuddyAdviceMethodSpec(true, enterAdviceMethod) );
                if (exitAdviceMethod != null)
                    adviceMethodSpecs.add( new ByteBuddyAdviceMethodSpec(false, exitAdviceMethod) );
                this.adviceMethodSpecs = adviceMethodSpecs;
            }


            /** 
             * Returns the Buddy advice kind (OnMethodEnter, OnMethodExit, or both). 
             */
            @Override
            public ByteBuddyAdviceKind getAdviceKind() {
                return (ByteBuddyAdviceKind) super.getAdviceKind();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public List<ByteBuddyAdviceMethodSpec> getAdviceMethodSpecs() {
                return adviceMethodSpecs;
            }


//            /** 
//             * {@inheritDoc}
//             */
//            @Override
//            public AdviceSpec load(TypePool targetTypePool) {
//                return this;
//            }
        }
    }
}