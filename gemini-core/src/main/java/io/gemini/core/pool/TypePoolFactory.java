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
package io.gemini.core.pool;

import java.util.concurrent.ConcurrentMap;

import io.gemini.api.classloader.AopClassLoader;
import io.gemini.core.concurrent.ConcurrentReferenceHashMap;
import io.gemini.core.pool.ExplicitTypePool.DelegatedTypeDescription;
import io.gemini.core.util.ClassLoaderUtils;
import net.bytebuddy.agent.builder.AgentBuilder.CircularityLock;
import net.bytebuddy.agent.builder.AgentBuilder.DescriptionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.LocationStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.PoolStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.pool.TypePool.Resolution;
import net.bytebuddy.utility.JavaModule;

/**
 * Factory for ByteBuddy {@link TypePool} instances used during class scanning and bytecode transformation.
 * <p>
 * Implements both {@link PoolStrategy} and {@link DescriptionStrategy} so it can be plugged directly
 * into ByteBuddy's {@link net.bytebuddy.agent.builder.AgentBuilder}.
 * Maintains a per-class-loader cache of {@link TypePool} instances.
 * </p>
 *
 * @author   martin.liu
 */
public interface TypePoolFactory {

    /**
     * Returns the {@link DescriptionStrategy} for use with ByteBuddy's AgentBuilder.
     *
     * @return the description strategy
     */
    DescriptionStrategy getDescriptionStrategy();

    /**
     * Returns the {@link PoolStrategy} for use with ByteBuddy's AgentBuilder.
     *
     * @return the pool strategy
     */
    PoolStrategy getPoolStrategy();

    /**
     * Returns the {@link LocationStrategy} used to locate class files.
     *
     * @return the location strategy
     */
    LocationStrategy getLocationStrategy();

    /**
     * Creates or retrieves a cached {@link TypePool} for the given class loader and module.
     *
     * @param classLoader the class loader to create a pool for
     * @param javaModule  the Java module (may be {@code null})
     * @return the type pool
     */
    TypePool createTypePool(ClassLoader classLoader, JavaModule javaModule);

    /**
     * Registers a existing type pool for the given class loader and module.
     * 
     * @param classLoader the class loader to create a pool for
     * @param javaModule  the Java module (may be {@code null})
     * @param typePool the type pool to be registered
     */
    void registerTypePool(ClassLoader classLoader, JavaModule javaModule, TypePool typePool);

    /**
     * Removes and returns the cached type resolution for the given type name.
     *
     * @param typeName the fully-qualified type name
     * @return the removed resolution, or {@code null} if not cached
     */
    Resolution removeTypeResolution(ClassLoader classLoader, JavaModule javaModule, String typeName);


    /**
     * Default implementation of {@link TypePoolFactory} that caches one {@link TypePool}
     * per class loader and implements both {@link PoolStrategy} and {@link DescriptionStrategy}
     * for direct use with ByteBuddy's {@link net.bytebuddy.agent.builder.AgentBuilder}.
     */
    class Default implements PoolStrategy, DescriptionStrategy, TypePoolFactory {

        private final LocationStrategy locationStrategy;

        private final ConcurrentMap<ClassLoader, TypePool> typePools;


        /**
         * Constructs a {@code Default} factory with the default weak class-file location strategy.
         */
        public Default() {
            this(null);
        }

        /**
         * Constructs a {@code Default} factory with the given location strategy.
         *
         * @param locationStrategy the strategy for locating class files (may be {@code null} for default)
         */
        public Default(LocationStrategy locationStrategy) {
            this.locationStrategy = locationStrategy == null ? LocationStrategy.ForClassLoader.WEAK : locationStrategy;
            this.typePools = new ConcurrentReferenceHashMap<>();
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public PoolStrategy getPoolStrategy() {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DescriptionStrategy getDescriptionStrategy() {
            return this;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public LocationStrategy getLocationStrategy() {
            return this.locationStrategy;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader) {
            TypePool typePool = createTypePool(classLoader, null);

            return facade( typePool );
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypePool typePool(ClassFileLocator classFileLocator, ClassLoader classLoader, String name) {
            TypePool typePool = createTypePool(classLoader, null);

            if (typePool instanceof ExplicitTypePool && name != null)
                ((ExplicitTypePool) typePool).addClassFileLocator(name, classFileLocator);

            return facade(typePool);
        }

        protected TypePool facade(TypePool typePool) {
            return new TypePool.LazyFacade(typePool);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public boolean isLoadedFirst() {
            return true;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypeDescription apply(String typeName, Class<?> type, TypePool typePool, 
                CircularityLock circularityLock, ClassLoader classLoader, JavaModule module) {
            TypePool originTypePool = createTypePool(classLoader, module);
            boolean isExplicitTypePool = originTypePool instanceof ExplicitTypePool;

            // find the described type with its name
            if (isExplicitTypePool) {
                Resolution resolution = ((ExplicitTypePool) originTypePool).find(typeName);
                if (resolution != null && resolution.isResolved())
                    return resolution.resolve();
            }

            // cache loaded type
            if (type != null) {
                TypeDescription typeDescription = TypeDescription.ForLoadedType.of(type);
                Resolution resolution = doCreateResolution(typeDescription);

                if (isExplicitTypePool)
                    ((ExplicitTypePool) originTypePool).addTypeResolution(typeName, resolution);
            }

            return typePool.describe(typeName).resolve();
        }

        protected Resolution doCreateResolution(TypeDescription typeDescription) {
            return new Resolution.Simple(typeDescription);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public TypePool createTypePool(ClassLoader classLoader, JavaModule javaModule) {
            return this.typePools.computeIfAbsent(
                    ClassLoaderUtils.maskNull(classLoader), 
                    key -> doCreateTypePool(classLoader, javaModule) 
            );
        }

        protected TypePool doCreateTypePool(ClassLoader classLoader, JavaModule javaModule) {
            if (classLoader instanceof AopClassLoader)
                // reuse loaded Aop framework classes for better performance
                return TypePool.ClassLoading.of(classLoader);
            else {
                // eagerly look up TypeDescription of target type.
                return new ExplicitTypePool.Default(
                        ClassLoaderUtils.getClassLoaderName(classLoader), 
                        ClassFileLocator.ForClassLoader.of(classLoader)
                ); 
            }
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public void registerTypePool(ClassLoader classLoader, JavaModule javaModule, TypePool typePool) {
            TypePool existingTypePool = this.typePools.putIfAbsent(ClassLoaderUtils.maskNull(classLoader), typePool);
            if (existingTypePool != null)
                throw new IllegalStateException(
                        String.format("Already registered type pool %s for class loader %s", existingTypePool, classLoader) );
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public Resolution removeTypeResolution(ClassLoader classLoader, JavaModule javaModule, String typeName) {
            TypePool originTypePool = createTypePool(classLoader, javaModule);
            if (!(originTypePool instanceof ExplicitTypePool))
                return null;

            ExplicitTypePool explicitTypePool = (ExplicitTypePool) originTypePool;
            Resolution resolution = explicitTypePool.removeTypeResolution(typeName);
            explicitTypePool.removeClassFileLocator(typeName);

            return resolution;
        }


        /**
         * Extends {@link TypePoolFactory.Default} to wrap type resolutions in
         * {@link io.gemini.core.pool.TypePools.DelegatedResolution} instances that track
         * which type properties are accessed during pointcut matching.
         */
        public static class TypeResolutionDetector extends TypePoolFactory.Default {

            /**
             * Constructs a {@code TypeResolutionDetector} with the given location strategy.
             *
             * @param locationStrategy the strategy for locating class files
             */
            public TypeResolutionDetector(LocationStrategy locationStrategy) {
                super(locationStrategy);
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            protected TypePool facade(TypePool typePool) {
                return typePool;
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            protected Resolution doCreateResolution(TypeDescription typeDescription) {
                return new Resolution.Simple(
                        new DelegatedTypeDescription(typeDescription.getName(), () -> typeDescription));
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            protected TypePool doCreateTypePool(ClassLoader classLoader, JavaModule javaModule) {
                if (classLoader instanceof AopClassLoader)
                    // reuse loaded Aop framework classes for better performance
                    return new TypePool.LazyFacade( TypePool.ClassLoading.of(classLoader));
                else {
                    // eagerly look up TypeDescription of target type.
                    return new ExplicitTypePool.TypeResolutionDetector(
                            ClassLoaderUtils.getClassLoaderName(classLoader),
                            ClassFileLocator.ForClassLoader.of(classLoader)
                    ); 
                }
            }
        }
    }
}
