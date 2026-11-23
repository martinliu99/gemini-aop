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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import io.gemini.aspectj.weaver.PointcutParameter.ParameterCategory;
import io.gemini.core.config.ConfigView;
import io.gemini.core.util.MethodUtils;
import io.gemini.core.util.Pair;
import io.gemini.core.util.SignatureParser;
import io.gemini.core.util.SignatureParser.MethodSignature;
import io.gemini.core.util.SignatureParser.TypeSignature;
import io.gemini.core.util.StringUtils;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.pool.TypePool;

/**
 * Describes the metadata of an advice method or class parsed from an aspect
 * application.
 * <p>
 * Three sub-interfaces cover the three advice styles:
 * <ul>
 * <li>{@link PojoAdviceSpec} – POJO before/after/around advice</li>
 * <li>{@link AspectJAdviceSpec} – AspectJ annotation-based advice with pointcut
 * parameter binding</li>
 * <li>{@link ByteBuddyAdviceSpec} – raw ByteBuddy {@code @Advice} class</li>
 * </ul>
 * </p>
 *
 * @author martin.liu
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
         * @param aspectTypePool the aspect {@link TypePool} per target class loader
         * @return the instance of {@link AdviceSpec}
         */
        T load(TypePool aspectTypePool);

    }

    /**
     * Abstract base providing common advice spec properties: advice kind, declaring
     * type, advice class name, advice method, and parameterized return/throw types.
     */
    abstract class AbstractBase implements AdviceSpec {

        protected static final Logger LOGGER = LoggerFactory.getLogger(AdviceSpec.class);

        protected static final Pair<String, String> UNDEFIEND_NAME_PAIR = new Pair<String, String>(null, null);
        protected static final Pair<TypeDescription, TypeDescription> UNDEFIEND_TYPE_PAIR = new Pair<TypeDescription, TypeDescription>(
                null, null);
        protected static final TypeDescription JOINPOINT = TypeDescription.ForLoadedType
                .of(Joinpoint.MutableJoinpoint.class);

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

        protected MethodDescription loadAdviceMethod(TypePool aspectTypePool, TypeDescription declaringType,
                MethodDescription adviceMethod) {
            if (declaringType == null) {
                // load type actually defining advice method
                String declaringTypeName = adviceMethod.getDeclaringType().getTypeName();
                declaringType = aspectTypePool.describe(declaringTypeName).resolve();
            }

            return declaringType.getDeclaredMethods()
                    .filter(named(adviceMethod.getName()).and(hasDescriptor(adviceMethod.getDescriptor()))).getOnly();
        }

        protected Generic getMutableJoinpointType(MethodDescription adviceMethod) {
            ParameterList<?> parameters = adviceMethod.getParameters();
            if (parameters.size() == 0)
                return null;

            Generic firstParameter = parameters.get(0).getType();
            if (!JOINPOINT.isAssignableFrom(firstParameter.asErasure())
                    || TypeDefinition.Sort.PARAMETERIZED != firstParameter.getSort())
                return null;

            return firstParameter.getTypeArguments().size() > 0 ? firstParameter : null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
            return this.declaringType != null ? this.declaringType.getDeclaredAnnotations().ofType(annotationClass)
                    : null;
        }

        protected String toString(String declaringTypeName, MethodDescription methodDescription) {
            StringBuilder stringBuilder = new StringBuilder();
            int modifiers = methodDescription.getModifiers();
            stringBuilder.append(Modifier.toString(modifiers)).append(' ');

            MethodSignature methodSignature = createMethodSignature(methodDescription);
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
                stringBuilder.append(typeSignature.getGenericName());
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
         * Subtype of {@link PojoAdviceSpec} holds metadata used to load
         * {@link PojoAdviceSpec.Loaded} instance per target {@link TypePool}.
         */
        interface Unloaded extends PojoAdviceSpec, Loadable<PojoAdviceSpec.Loaded> {

            /**
             * The default implementation of {@link PojoAdviceSpec.Unloaded}.
             */
            class Default extends AbstractBase implements Unloaded {

                private final MethodDescription adviceMethod;


                /**
                 * Creates a POJO advice spec.
                 *
                 * @param adviceKind    the POJO advice kind
                 * @param declaringType the declaring type name
                 * @param adviceMethod  the advice method (before/after/invoke)
                 */
                public Default(PojoAdviceKind adviceKind, TypeDescription declaringType,
                        MethodDescription adviceMethod) {
                    super(adviceKind, declaringType, declaringType.getTypeName());
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

                /**
                 * {@inheritDoc}
                 */
                @Override
                public PojoAdviceSpec.Loaded load(TypePool aspectTypePool) {
                    return new PojoAdviceSpec.Loaded.Default(aspectTypePool, this);
                }
            }
        }

        /**
         * Subtype of {@link PojoAdviceSpec} parses metadata, e.g., type arguments of
         * joinpoint parameter which should be resolved per target {@link TypePool}.
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
             * Returns the joinpoint parameter.
             * 
             * @return the joinpoint parameter
             */
            Generic getMutableJoinpointType();


            /**
             * The default implementation of {@link PojoAdviceSpec.Loaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements PojoAdviceSpec.Loaded {

                private final TypePool aspectTypePool;
                private final PojoAdviceSpec.Unloaded.Default adviceSpec;

                private MethodDescription adviceMethod;


                public Default(TypePool aspectTypePool, PojoAdviceSpec.Unloaded.Default adviceSpec) {
                    super(adviceSpec.getAdviceKind(), null, adviceSpec.getAdviceClassName());

                    this.aspectTypePool = aspectTypePool;
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
                        this.setDeclaringType(aspectTypePool.describe(adviceSpec.getDeclaringTypeName()).resolve());
                    return super.getDeclaringType();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public MethodDescription getAdviceMethod() {
                    if (this.adviceMethod == null)
                        this.adviceMethod = loadAdviceMethod(aspectTypePool, null, adviceSpec.getAdviceMethod());

                    return this.adviceMethod;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Generic getMutableJoinpointType() {
                    return getMutableJoinpointType(getAdviceMethod());
                }
            }
        }
    }

    /**
     * AspectJ annotation-based advice spec.
     */
    interface AspectJAdviceSpec extends AdviceSpec {

        /**
         * Returns the AspectJ advice kind
         * (@Before, @After, @AfterReturning, @AfterThrowing, @Around).
         */
        @Override
        AspectJAdviceKind getAdviceKind();

        /**
         * Returns the AspectJ advice annotation (e.g., {@code @Before},
         * {@code @Around}).
         */
        AnnotationDescription getAdviceAnnotation();

        /**
         * Subtype of {@link AspectJAdviceSpec} holds metadata used to load
         * {@link AspectJAdviceSpec.Loaded} instance per target {@link TypePool}.
         */
        interface Unloaded extends AspectJAdviceSpec, Loadable<AspectJAdviceSpec.Loaded> {

            /**
             * The default implementation of {@link AspectJAdviceSpec.Unloaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements AspectJAdviceSpec.Unloaded {

                private static final List<String> ACCESSIBLE_OBJECTS = Arrays.asList(AccessibleObject.class.getName(),
                        Executable.class.getName(), Constructor.class.getName(), Method.class.getName());

                private final AnnotationDescription adviceAnnotation;

                private final MethodDescription adviceMethod;

                private List<String> aspectJArgNames;
                private List<ParameterCategory> parameterCategories;


                public Default(TypeDescription declaringType, MethodDescription adviceMethod,
                        AnnotationDescription adviceAnnotation) throws IllegalSpecException {
                    super(null, declaringType, null);

                    this.adviceMethod = adviceMethod;
                    this.adviceAnnotation = adviceAnnotation;

                    String adviceKindValue = adviceAnnotation.getAnnotationType().getSimpleName()
                            .toUpperCase(Locale.ENGLISH);
                    setAdviceKind(AspectJAdviceKind.parse(adviceKindValue));

                    setAdviceClassName(generateAdviceClassName(adviceKindValue, adviceMethod));

                    String argNamesStr = adviceAnnotation.getValue("argNames").resolve(String.class).trim();
                    String returningParameterName = getAdviceKind().isAfterReturning()
                            ? adviceAnnotation.getValue("returning").resolve(String.class).trim()
                            : null;
                    String throwingParameterName = getAdviceKind().isAfterThrowing()
                            ? adviceAnnotation.getValue("throwing").resolve(String.class).trim()
                            : null;

                    initialize(declaringType, adviceMethod, getAdviceKind(), argNamesStr,
                            returningParameterName, throwingParameterName);
                }

                public Default(TypeDescription declaringType, MethodDescription adviceMethod, ConfigView configView,
                        String configKeyPrefix) throws IllegalSpecException {
                    super(null, declaringType, null);

                    this.adviceMethod = adviceMethod;
                    this.adviceAnnotation = null;

                    String adviceKindKey = configKeyPrefix + "adviceCategory";
                    String adviceKindValue = configView.getAsString(adviceKindKey, "").toUpperCase(Locale.ENGLISH);
                    try {
                        setAdviceKind(AspectJAdviceKind.parse(adviceKindValue));
                    } catch (Exception e) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ advice method with illegal 'adviceKind' configuration property. \n"
                                            + "  ConfigKeyPrefix: {} \n" + "  DeclaringType: {} \n"
                                            + "  AdviceMethod: {} \n" + "    {}: {} \n" + "  Error reason: {} \n",
                                    configKeyPrefix, declaringType,
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    adviceKindKey, adviceKindValue, e.getMessage());

                        throw new IllegalSpecException();
                    }

                    setAdviceClassName(generateAdviceClassName(adviceKindValue, adviceMethod));

                    String argNamesStr = configView.getAsString(configKeyPrefix + "argNames", "");
                    String returningParameterName = getReturingParameterName(configView, configKeyPrefix);
                    String throwingParameterName = getThrowingParameterName(configView, configKeyPrefix);

                    initialize(declaringType, adviceMethod, getAdviceKind(), argNamesStr,
                            returningParameterName, throwingParameterName);
                }

                private String getReturingParameterName(ConfigView configView, String configKeyPrefix) {
                    String returningKey = configKeyPrefix + "returning";
                    String returningParameterName = configView.getAsString(returningKey, "");
                    if (getAdviceKind().isAfterReturning())
                        return returningParameterName;

                    if (LOGGER.isInfoEnabled() && StringUtils.hasText(returningParameterName))
                        LOGGER.info(
                                "Ignored meaningless 'returning' property of non AspectJ @AfterReturning advice method. \n"
                                        + "  ConfigKeyPrefix: {} \n" + "  DeclaringType: {} \n" + "    {}: {} \n",
                                configKeyPrefix, getDeclaringTypeName(), returningKey, returningParameterName);
                    return null;
                }

                private String getThrowingParameterName(ConfigView configView, String configKeyPrefix) {
                    String throwingKey = configKeyPrefix + "throwing";
                    String throwingParameterName = configView.getAsString(throwingKey, "");
                    if (getAdviceKind().isAfterThrowing())
                        return throwingParameterName;

                    if (LOGGER.isInfoEnabled() && StringUtils.hasText(throwingParameterName))
                        LOGGER.info(
                                "Ignored meaningless 'throwing' property of non AspectJ @AfterThrowing advice method. \n"
                                        + "  ConfigKeyPrefix: {} \n" + "  DeclaringType: {} \n" + "    {}: {} \n",
                                configKeyPrefix, getDeclaringTypeName(), throwingKey, throwingParameterName);
                    return null;
                }

                private String generateAdviceClassName(String adviceKind, MethodDescription adviceMethod) {
                    // define advice class name
                    return getDeclaringTypeName() + "_$$" + adviceMethod.getName() + "_" + adviceKind + "$$";
                }

                private void initialize(TypeDescription declaringType, MethodDescription adviceMethod, AspectJAdviceKind adviceKind, 
                        String argNamesStr, String returningParameterName, String throwingParameterName) {
                    ParameterList<?> parameters = adviceMethod.getParameters();
                    if (parameters.size() == 0) {
                        this.aspectJArgNames = new ArrayList<>();
                        this.parameterCategories = new ArrayList<>();

                        return;
                    }

                    // 1.resolve joinpoint parameter
                    List<ParameterCategory> parameterCategories = new ArrayList<>(
                            Collections.nCopies(parameters.size(), null));

                    resolveJoinpointParamBinding(adviceMethod, parameterCategories);

                    // 2.resolve the AspectJ 'argNames' attribute
                    List<String> aspectJArgNames = resolveAspectJArgNames(declaringType, adviceMethod, parameters,
                            argNamesStr);

                    // 3.resolve the AspectJ 'returning' and 'throwing' attribute
                    if (adviceKind.isAfterReturning())
                        resolveAdviceReturningParamBinding(parameterCategories, aspectJArgNames,
                                returningParameterName);

                    if (adviceKind.isAfterThrowing())
                        resolveAdviceThrowingParamBinding(parameterCategories, aspectJArgNames, throwingParameterName);

                    this.aspectJArgNames = aspectJArgNames;
                    this.parameterCategories = parameterCategories;
                }

                private boolean resolveJoinpointParamBinding(MethodDescription adviceMethod, List<ParameterCategory> parameterCategories) {
                    // bind first parameter
                    String parameterTypeName = Type.getMethodType(adviceMethod.getDescriptor())
                            .getArgumentTypes()[0]
                                    .getClassName();

                    ParameterCategory parameterCategory = null;
                    if (Joinpoint.class.getName().equals(parameterTypeName))
                        parameterCategory = ParameterCategory.JOINPOINT_PARAM;
                    else if (Joinpoint.MutableJoinpoint.class.getName().equals(parameterTypeName))
                        parameterCategory = ParameterCategory.MUTABLE_JOINPOINT_PARAM;
                    else if (ACCESSIBLE_OBJECTS.contains(parameterTypeName))
                        parameterCategory = ParameterCategory.STATIC_PART_PARAM;

                    if (parameterCategory != null) {
                        parameterCategories.add(0, parameterCategory);
                        return true;
                    } else
                        return false;
                }

                private List<String> resolveAspectJArgNames(TypeDescription declaringType,
                        MethodDescription adviceMethod, ParameterList<?> parameters, String argNamesStr) {
                    // parse 'argNames' value in annotation
                    StringTokenizer st = new StringTokenizer(argNamesStr, ",");
                    int argCount = st.countTokens();
                    if (argCount == 0) {
                        return parameters.stream().map(p -> p.getName()).collect(Collectors.toList());
                    }

                    // check argument count
                    if (argCount != parameters.size() && argCount != parameters.size() - 1) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ advice method with parameter count is inconsistent with 'argNames' annotation attribute. \n"
                                            + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                            + "    ArgNames: {} \n",
                                    declaringType,
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    argNamesStr);

                        throw new IllegalSpecException();
                    }

                    List<String> argNames = new ArrayList<>(argCount);
                    while (st.hasMoreTokens())
                        argNames.add(st.nextToken().trim());

                    if (argCount == parameters.size() - 1)
                        argNames.add(0, parameters.get(0).getName());

                    return argNames;
                }

                private void resolveAdviceReturningParamBinding(List<ParameterCategory> parameterCategories,
                        List<String> aspectJArgNames, String returningParameterName) {
                    // resolve returning parameters
                    if (!StringUtils.hasText(returningParameterName)) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ @AfterReturning advice method without 'returning' annotation attribute. \n"
                                            + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                            + "    'returning': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    returningParameterName);

                        throw new IllegalSpecException();
                    }

                    int pos = aspectJArgNames.indexOf(returningParameterName);
                    if (pos == -1) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ @AfterReturning advice method with 'returning' annotation attribute referring to nonexistent parameter. \n"
                                            + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                            + "    'returning': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    returningParameterName);

                        throw new IllegalSpecException();
                    }

                    parameterCategories.add(pos, ParameterCategory.RETURNING_ANNOTATION);
                }

                private void resolveAdviceThrowingParamBinding(List<ParameterCategory> parameterCategories,
                        List<String> aspectJArgNames, String throwingParameterName) {
                    // resolve throwing parameters
                    if (!StringUtils.hasText(throwingParameterName)) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ @AfterThrowing advice method without 'throwing' annotation attribute. \n"
                                            + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                            + "    'throwing': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    throwingParameterName);

                        throw new IllegalSpecException();
                    }

                    int pos = aspectJArgNames.indexOf(throwingParameterName);
                    if (pos == -1) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn(
                                    "Ignored AspectJ @AfterThrowing advice method with 'throwing' annotation attribute referring to nonexistent parameter. \n"
                                            + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                            + "    'throwing': {} \n",
                                    getDeclaringTypeName(),
                                    toString(getDeclaringTypeName(), adviceMethod),
                                    throwingParameterName);

                        throw new IllegalSpecException();
                    }

                    parameterCategories.add(pos, ParameterCategory.THROWING_ANNOTATION);
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
                    AnnotationDescription annotationDescription = this.adviceMethod.getDeclaredAnnotations()
                            .ofType(annotationClass);
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

                public List<String> getAspectJArgNames() {
                    return aspectJArgNames;
                }

                public List<ParameterCategory> getParameterCategories() {
                    return parameterCategories;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public AspectJAdviceSpec.Loaded load(TypePool aspectTypePool) {
                    return new AspectJAdviceSpec.Loaded.Default(aspectTypePool, this);
                }
            }
        }

        /**
         * Subtype of {@link AspectJAdviceSpec} parses the pointcut parameter bindings,
         * returning/throwing parameter types, and generates the unloaded advice class
         * via {@link AspectJAdviceGenerator}.
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
             * Returns type joinpoint parameter.
             * 
             * @return type joinpoint parameter
             */
            Generic getMutableJoinpointType();

            /**
             * Returns the map of pointcut parameter names to their type names, used for
             * parameter binding in the generated advice class.
             *
             * @return map of parameter name to generic type
             */
            Map<String, String> getPointcutParameters();

            /**
             * Returns the resolved advice method parameters with their categories (e.g.,
             * args, this, target, returning, throwing).
             *
             * @return list of {@link ResolvedParameter}
             */
            List<ResolvedParameter> getResolvedParameters();

            /**
             * Returns the dynamically generated, unloaded {@link Advice} class that wraps
             * the AspectJ advice method for use with the Gemini AOP framework dispatch
             * mechanism.
             *
             * @return the unloaded advice class
             */
            DynamicType.Unloaded<? extends Advice> getUnloadedAdviceClass();

            /**
             * The default implementation of {@link AspectJAdviceSpec.Loaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements AspectJAdviceSpec.Loaded {

                private final TypePool aspectTypePool;
                private final AspectJAdviceSpec.Unloaded.Default adviceSpec;

                private MethodDescription adviceMethod;

                private Map<String, String> pointcutParameters;
                private List<ResolvedParameter> resolvedParameters;

                private DynamicType.Unloaded<? extends Advice> unloadedAdviceClass;


                public Default(TypePool aspectTypePool, AspectJAdviceSpec.Unloaded.Default adviceSpec)
                        throws IllegalSpecException {
                    super(adviceSpec.getAdviceKind(), null, adviceSpec.getAdviceClassName());

                    this.aspectTypePool = aspectTypePool;
                    this.adviceSpec = adviceSpec;
                    this.resolvedParameters = new ArrayList<>();
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
                        this.setDeclaringType(aspectTypePool.describe(adviceSpec.getDeclaringTypeName()).resolve());
                    return super.getDeclaringType();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public AnnotationDescription getAnnotation(Class<? extends Annotation> annotationClass) {
                    AnnotationDescription annotationDescription = this.adviceMethod.getDeclaredAnnotations()
                            .ofType(annotationClass);
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
                        this.adviceMethod = loadAdviceMethod(aspectTypePool, getDeclaringType(),
                                adviceSpec.getAdviceMethod());

                    return this.adviceMethod;
                }


                /**
                 * {@inheritDoc}
                 */
                @Override
                public Generic getMutableJoinpointType() {
                    return getMutableJoinpointType(getAdviceMethod());
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Map<String, String> getPointcutParameters() {
                    if (this.pointcutParameters != null)
                        return this.pointcutParameters;

                    List<String> aspectJArgNames = adviceSpec.getAspectJArgNames();
                    Map<String, String> pointcutParameters = new LinkedHashMap<>(aspectJArgNames.size());
                    int index = 0;
                    for (ParameterDescription parameter : getAdviceMethod().getParameters()) {
                        pointcutParameters.put(aspectJArgNames.get(index++),
                                parameter.getType().asErasure().getTypeName());
                    }

                    return this.pointcutParameters = pointcutParameters;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean match(MethodDescription targetMethod, List<PointcutParameter> pointcutParameters) {
                    // collect resolved parameters of advice method
                    ParameterList<?> adviceMethodParameters = getAdviceMethod().getParameters();
                    List<ResolvedParameter> resolvedParameters = new ArrayList<>(adviceMethodParameters.size());
                    ParameterList<?> targetMethodParameters = targetMethod.getParameters();

                    int parameterCategoryIndex = 0, pointcutParameterIndex = 0;
                    for (ParameterDescription parameter : adviceMethodParameters) {
                        Generic parameterType = parameter.getType();
                        ParameterCategory parameterCategory = adviceSpec.getParameterCategories()
                                .get(parameterCategoryIndex++);

                        if (parameterCategory != null) {
                            Generic matchedType = parameterType;

                            switch (parameterCategory) {
                            case RETURNING_ANNOTATION: {
                                matchedType = targetMethod.getReturnType();
                                break;
                            }
                            case THROWING_ANNOTATION: {
                                // TODO:
                                break;
                            }
                            default:
                                break;
                            }

                            resolvedParameters.add(new ResolvedParameter.Default(parameter.getName(), parameterCategory,
                                    matchedType, parameterType));
                        } else {
                            PointcutParameter pointcutParameter = pointcutParameterIndex < pointcutParameters.size()
                                    ? pointcutParameters.get(pointcutParameterIndex++)
                                    : null;
                            if (pointcutParameter == null) {
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn(
                                            "Ignored advice method with parameter '{}' could not bind to target method's parameter. \n"
                                                    + "  DeclaringType: {} \n" + "  AdviceMethod: {} \n"
                                                    + "  TargetMethod: {} \n",
                                            parameter.getName(), adviceSpec.getDeclaringTypeName(),
                                            MethodUtils.getMethodSignature(adviceMethod),
                                            MethodUtils.getMethodSignature(targetMethod));
                                }

                                return false;
                            }

                            Generic matchedType = null;
                            if (pointcutParameter.getArgsIndex() >= 0)
                                matchedType = targetMethodParameters.get(pointcutParameter.getArgsIndex()).getType();
                            else
                                matchedType = aspectTypePool.describe(pointcutParameter.getMacthedTypeName()).resolve()
                                        .asGenericType();

                            resolvedParameters.add(new ResolvedParameter.Default(parameter.getName(), pointcutParameter,
                                    matchedType, parameterType));
                        }
                    }

                    this.resolvedParameters = resolvedParameters;

                    return true;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public List<ResolvedParameter> getResolvedParameters() {
                    return resolvedParameters;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public DynamicType.Unloaded<? extends Advice> getUnloadedAdviceClass() {
                    if (unloadedAdviceClass == null)
                        this.unloadedAdviceClass = AspectJAdviceGenerator.INSTANCE.make(this, true);

                    return unloadedAdviceClass;
                }
            }
        }
    }

    /**
     * Raw ByteBuddy {@code @Advice} class spec, incuding enterAdviceMethod and
     * exitAdviceMethod.
     */
    interface ByteBuddyAdviceSpec extends AdviceSpec {

        /**
         * Returns the ByteBuddy advice kind (ON_METHOD_ENTER, ON_METHOD_EXIT, or
         * ON_METHOD_ENTER_EXIT).
         *
         * @return the {@link PojoAdviceKind}
         */
        @Override
        ByteBuddyAdviceKind getAdviceKind();

        /**
         * Subtype of {@link ByteBuddyAdviceSpec} holds metadata used to load
         * {@link ByteBuddyAdviceSpec.Loaded} instance per target {@link TypePool}.
         */
        interface Unloaded extends ByteBuddyAdviceSpec, Loadable<ByteBuddyAdviceSpec.Loaded> {

            /**
             * The default implementation of {@link ByteBuddyAdviceSpec.Unloaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements ByteBuddyAdviceSpec.Unloaded {

                private final MethodDescription enterAdviceMethod;
                private final MethodDescription exitAdviceMethod;

                public Default(AdviceKind adviceKind, TypeDescription declaringType,
                        MethodDescription enterAdviceMethod, MethodDescription exitAdviceMethod) {
                    super(adviceKind, declaringType, declaringType.getTypeName());

                    this.enterAdviceMethod = enterAdviceMethod;
                    this.exitAdviceMethod = exitAdviceMethod;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public ByteBuddyAdviceKind getAdviceKind() {
                    return (ByteBuddyAdviceKind) super.getAdviceKind();
                }

                public MethodDescription getEnterAdviceMethod() {
                    return enterAdviceMethod;
                }

                public MethodDescription getExitAdviceMethod() {
                    return exitAdviceMethod;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Loaded load(TypePool aspectTypePool) {
                    return new ByteBuddyAdviceSpec.Loaded.Default(aspectTypePool, this);
                }
            }
        }

        /**
         * Subtype of {@link ByteBuddyAdviceSpec} parses metadata, which should be
         * resolved per target {@link TypePool}.
         */
        interface Loaded extends ByteBuddyAdviceSpec {

            /**
             * Return the enter advice method description.
             * 
             * @return the enter advice {@link MethodDescription}.
             */
            MethodDescription getEnterAdviceMethod();

            /**
             * Return the exit advice method description.
             * 
             * @return the exit advice {@link MethodDescription}.
             */
            MethodDescription getExitAdviceMethod();

            /**
             * The default implementation of {@link ByteBuddyAdviceSpec.Loaded}.
             */
            class Default extends AdviceSpec.AbstractBase implements ByteBuddyAdviceSpec.Loaded {

                private final TypePool aspectTypePool;
                private final ByteBuddyAdviceSpec.Unloaded.Default adviceSpec;

                private MethodDescription enterAdviceMethod;
                private MethodDescription exitAdviceMethod;

                public Default(TypePool aspectTypePool, ByteBuddyAdviceSpec.Unloaded.Default adviceSpec)
                        throws IllegalSpecException {
                    super(adviceSpec.getAdviceKind(), null, adviceSpec.getAdviceClassName());

                    this.aspectTypePool = aspectTypePool;
                    this.adviceSpec = adviceSpec;

                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public ByteBuddyAdviceKind getAdviceKind() {
                    return (ByteBuddyAdviceKind) super.getAdviceKind();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public TypeDescription getDeclaringType() {
                    if (super.getDeclaringType() == null)
                        this.setDeclaringType(aspectTypePool.describe(adviceSpec.getDeclaringTypeName()).resolve());
                    return super.getDeclaringType();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public MethodDescription getEnterAdviceMethod() {
                    if (this.enterAdviceMethod == null && adviceSpec.getEnterAdviceMethod() != null)
                        this.enterAdviceMethod = loadAdviceMethod(aspectTypePool, getDeclaringType(),
                                adviceSpec.getEnterAdviceMethod());

                    return this.enterAdviceMethod;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public MethodDescription getExitAdviceMethod() {
                    if (this.exitAdviceMethod == null && adviceSpec.getExitAdviceMethod() != null)
                        this.exitAdviceMethod = loadAdviceMethod(aspectTypePool, getDeclaringType(),
                                adviceSpec.getExitAdviceMethod());

                    return this.exitAdviceMethod;
                }
            }
        }
    }
}