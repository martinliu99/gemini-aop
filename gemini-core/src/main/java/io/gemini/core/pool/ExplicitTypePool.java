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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import io.gemini.core.util.StringUtils;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;

/**
 * A type pool extends {@link TypePool}, and explicitly defines {@link TypePool.Resolution}
 * and {@link ClassFileLocator} with the given type name.
 *
 * @author   martin.liu
 */
public interface ExplicitTypePool extends TypePool {

    /** 
     * {@inheritDoc}
     */
    @Override
    TypePool.Resolution describe(String typeName);

    /**
     * Finds the described type by its name.
     *
     * @param typeName The name of the type to find. The name is to be written as when calling {@link Class#getName()}.
     * @return A resolution of the type to describe. If the type to be described was found, the returned
     * {@link net.bytebuddy.pool.TypePool.Resolution} represents this type. Otherwise, an illegal resolution is returned.
     */
    TypePool.Resolution find(String typeName);


    /**
     * Adds a type resolution to this pool.
     *
     * @param typeName   the fully-qualified type name
     * @param resolution the resolution to cache
     */
    void addTypeResolution(String typeName, TypePool.Resolution resolution);

    /**
     * Removes and returns the cached resolution for the given type name.
     * 
     * @param typeName the fully-qualified type name
     * @return the removed resolution, or {@code null} if not cached
     */
    TypePool.Resolution removeTypeResolution(String typeName);


    /**
     * Adds a class file locator to this pool.
     * 
     * @param typeName   the fully-qualified type name
     * @param classFileLocator the classFileLocator to add
     */
    void addClassFileLocator(String typeName, ClassFileLocator classFileLocator);

    /**
     * Removes and returns the class file locator for the given type name.
     * 
     * @param typeName   the fully-qualified type name
     * @return the removed classFileLocator, or {@code null} if not exist
     */
    ClassFileLocator removeClassFileLocator(String typeName);


    /**
     * The default implementation of {@link ExplicitTypePool}.
     *
     */
    class Default extends TypePool.Default.WithLazyResolution implements ExplicitTypePool {

        private final String poolName;
        private final ConcurrentMap<String, TypePool.Resolution> resolutions;


        public Default(String poolName, ClassFileLocator classFileLocator) {
            this(poolName, new CacheProvider.Simple.UsingSoftReference(), classFileLocator);
        }

        public Default(String poolName, CacheProvider cacheProvider, ClassFileLocator classFileLocator) {
            super(cacheProvider, new Mutable(classFileLocator), ReaderMode.FAST, LazinessMode.EXTENDED);

            this.poolName = poolName;
            this.resolutions = new ConcurrentHashMap<>();
        }


        public String getPoolName() {
            return poolName;
        }

        protected CacheProvider getCacheProvider() {
            return this.cacheProvider;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public TypePool.Resolution describe(String typeName) {
            TypePool.Resolution resolution = find(typeName);
            return resolution != null ? resolution : super.describe(typeName);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypePool.Resolution find(String name) {
            TypePool.Resolution resolution = this.resolutions.get(name);
            if (resolution != null)
                return resolution;

            return getCacheProvider().find(name);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public void addTypeResolution(String typeName, TypePool.Resolution resolution) {
            if (!StringUtils.hasText(typeName) || resolution == null) return;

            this.resolutions.put(typeName, resolution);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public TypePool.Resolution removeTypeResolution(String typeName) {
            if (!StringUtils.hasText(typeName)) return null;

            return resolutions.remove(typeName);
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public void addClassFileLocator(String typeName, ClassFileLocator classFileLocator) {
            getClassFileLocator().addClassFileLocator(typeName, classFileLocator);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        public ClassFileLocator removeClassFileLocator(String typeName) {
            return getClassFileLocator().removeClassFileLocator(typeName);
        }

        private Mutable getClassFileLocator() {
            return (Mutable) super.classFileLocator;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return poolName;
        }


        /**
         * A {@link ClassFileLocator} contains several locators which could be modified at runtime.
         * Queries classFileLocatorMap by the type name firstly, otherwise locate class file by defaultClassFileLocator.
         *
         */
        protected static class Mutable implements ClassFileLocator {

            private final ConcurrentMap<String, ClassFileLocator> classFileLocatorMap;

            private final ClassFileLocator defaultClassFileLocator;


            public Mutable(ClassFileLocator classFileLocator) {
                this.classFileLocatorMap = new ConcurrentHashMap<String, ClassFileLocator>();

                this.defaultClassFileLocator = classFileLocator;
            }

            /**
             * Adds a class file locator to this composite locator.
             * 
             * @param typeName   the fully-qualified type name
             * @param classFileLocator the classFileLocator to add
             */
            public void addClassFileLocator(String typeName, ClassFileLocator classFileLocator) {
                if (typeName == null || classFileLocator == null)
                    return;
                this.classFileLocatorMap.put(typeName, classFileLocator);
            }

            /**
             * Removes and returns the class file locator for the given type name.
             * 
             * @param typeName   the fully-qualified type name
             * @return the removed classFileLocator, or {@code null} if not exist
             */
            public ClassFileLocator removeClassFileLocator(String typeName) {
                return this.classFileLocatorMap.remove(typeName);
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            public ClassFileLocator.Resolution locate(String name) throws IOException {
                ClassFileLocator classFileLocator = this.classFileLocatorMap.get(name);
                if (classFileLocator == null)
                    classFileLocator = this.defaultClassFileLocator;

                return classFileLocator.locate(name);
            }

            /** 
             * {@inheritDoc}
             */
            @Override
            public void close() throws IOException {
                for (ClassFileLocator classFileLocator : this.classFileLocatorMap.values())
                    classFileLocator.close();
                this.classFileLocatorMap.clear();

                this.defaultClassFileLocator.close();
            }
        }
    }


    /**
     * Extends {@link TypePools.Default} to wrap each type resolution in a
     * {@link DelegatedResolution} that tracks which type properties were accessed
     * (for type resolution performance analysis).
     */
    public static class TypeResolutionDetector extends ExplicitTypePool.Default {

        private final CacheProvider localCacheProvider;


        /**
         * Constructs a {@code TypeResolutionDetector}.
         *
         * @param poolName         a human-readable name for this pool
         * @param classFileLocator the locator for class files
         */
        public TypeResolutionDetector(String poolName, ClassFileLocator classFileLocator) {
            super(poolName, CacheProvider.NoOp.INSTANCE, classFileLocator);

            this.localCacheProvider = new CacheProvider.Simple.UsingSoftReference();
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        protected CacheProvider getCacheProvider() {
            return this.localCacheProvider;
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        protected TypePool.Resolution doDescribe(String typeName) {
            // resolve type lazily to improve type matching performance
            Supplier<TypeDescription> resolutionSupplier = new Supplier<TypeDescription>() {

                // cache resolved type
                private TypeDescription resolution;

                @Override
                public TypeDescription get() {
                    if (resolution == null)
                        resolution = doResolve(typeName).resolve();

                    return resolution;
                }
            };

            return new DelegatedResolution(typeName, resolutionSupplier);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        protected TypePool.Resolution doCache(String name, TypePool.Resolution resolution) {
            return getCacheProvider().register(name, resolution);
        }


        /**
         * A delegated resolution that holds a resolution supplier and a {@link DelegatedTypeDescription}.
         */
        static class DelegatedResolution implements TypePool.Resolution {

            private DelegatedTypeDescription delegatedTypeDescription;


            /**
             * Constructs a {@code DelegatedResolution} with a default {@link DelegatedTypeDescription}.
             *
             * @param typeName             the type name
             * @param delegateSupplier the supplier that provides the actual resolution
             */
            public DelegatedResolution(String typeName, Supplier<TypeDescription> delegateSupplier) {
                this.delegatedTypeDescription = new DelegatedTypeDescription(typeName, delegateSupplier);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isResolved() {
                try {
                    delegatedTypeDescription.delegate();
                return true;
                } catch (NoSuchTypeException e) {
                    return false;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public TypeDescription resolve() {
                return delegatedTypeDescription;
            }
        }
    }


    /**
     * A type description that delegates to another resolution supplier once a property
     * other than the type name is accessed.
     */
    class DelegatedTypeDescription extends TypeDescription.AbstractBase.OfSimpleType.WithDelegation
            implements TypeResolutionInspector {

        private final String typeName;
        private final Supplier<TypeDescription> delegateSupplier;

        private ResolutionLevel resolutionLevel = ResolutionLevel.NO_RESOLUTION;


        /**
         * Constructs a {@code DelegatedTypeDescription} for the given type name.
         *
         * @param typeName             the type name
         * @param delegateSupplier the supplier that provides the actual resolution
         */
        protected DelegatedTypeDescription(String typeName, Supplier<TypeDescription> delegateSupplier) {
            this.typeName = typeName;
            this.delegateSupplier = delegateSupplier;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return typeName;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void resetInspection() {
            this.resolutionLevel = ResolutionLevel.NO_RESOLUTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ResolutionLevel getResolutionLevel() {
            return resolutionLevel;
        }

        /** 
         * {@inheritDoc} 
         */
        @Override
        public void setResolutionLevel(ResolutionLevel resolutionLevel) {
            if (this.resolutionLevel.getLevelCode() < resolutionLevel.getLevelCode())
                this.resolutionLevel = resolutionLevel;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        protected TypeDescription delegate() {
            this.setResolutionLevel(ResolutionLevel.TYPE_RESOLUTION);

            return delegateSupplier.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Generic getSuperClass() {
            this.setResolutionLevel(ResolutionLevel.SUPER_TYPE_RESOLUTION);

            return delegateSupplier.get().getSuperClass();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TypeList.Generic getInterfaces() {
            this.setResolutionLevel(ResolutionLevel.SUPER_TYPE_RESOLUTION);

            return delegateSupplier.get().getInterfaces();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return typeName;
        }
    }
}