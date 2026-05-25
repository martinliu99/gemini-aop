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
package io.gemini.aop.factory.classloader;

import io.gemini.core.pool.TypePoolFactory;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;


/**
 * A ByteBuddy {@link TypePool} scoped to an aspect application.
 * <p>
 * Resolves type descriptions by first consulting the aspect class loader's own classpath,
 * then falling back to the target class loader's type pool. 
 * </p>
 *
 * @author   martin.liu
 */
public class AspectTypePool extends TypePool.Default {

    private final AspectClassLoader aspectClassLoader;
    private final TypePoolFactory typePoolFactory;


    public AspectTypePool(AspectClassLoader aspectClassLoader, TypePoolFactory typePoolFactory) {
        super(new CacheProvider.Simple.UsingSoftReference(), ClassFileLocator.ForClassLoader.of(aspectClassLoader), ReaderMode.FAST);

        this.aspectClassLoader = aspectClassLoader;
        this.typePoolFactory = typePoolFactory;
    }

    protected AspectClassLoader getAspectClassLoader() {
        return aspectClassLoader;
    }


    protected TypePoolFactory getTypePoolFactory() {
        return typePoolFactory;
    }

    public CacheProvider getCacheProvider() {
        return this.cacheProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }


    public static class PerTargetClassLoader extends AspectTypePool {

        public PerTargetClassLoader(AspectClassLoader.PerTargetClassLoader aspectClassLoader, TypePoolFactory typePoolFactory) {
            super(aspectClassLoader, typePoolFactory);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        protected AspectClassLoader.PerTargetClassLoader getAspectClassLoader() {
            return (AspectClassLoader.PerTargetClassLoader) super.getAspectClassLoader();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public TypePool.Resolution describe(String name) {
            // 1.look up cache
            TypePool.Resolution resolution = this.cacheProvider.find(name);
            if (resolution != null && resolution.isResolved())
                return resolution;

            // 2.look up via AspectClassLoader
//            try {
//                resolution = super.describe(name);
//                if (resolution != null && resolution.isResolved())
//                    return resolution;
//            } catch (Exception ignored) { /* do nothing */ }
//
//
//            // 3.look up cached target types including runtime generated via target type pool
//            ClassLoader targetCL = getAspectClassLoader().getTargetClassLoader();
//            TypePool typePool = this.getTypePoolFactory().createTypePool(targetCL, null);
//            return typePool.describe(name);

            
            // 2.look up via AspectClassLoader
            try {
                // 3.look up cached target types including runtime generated via target type pool
                ClassLoader targetCL = getAspectClassLoader().getTargetClassLoader();
                TypePool typePool = this.getTypePoolFactory().createTypePool(targetCL, null);
                resolution = typePool.describe(name);
                if (resolution != null && resolution.isResolved())
                    return resolution;

            } catch (Exception ignored) { /* do nothing */ }


            return super.describe(name);
        }
    }
}
