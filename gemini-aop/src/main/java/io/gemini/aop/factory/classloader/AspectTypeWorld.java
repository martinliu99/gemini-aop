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
/**
 * 
 */
package io.gemini.aop.factory.classloader;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

import io.gemini.aspectj.weaver.TypeWorld;
import io.gemini.aspectj.weaver.TypeWorldFactory;
import io.gemini.aspectj.weaver.UnresolvedAspectType;
import io.gemini.aspectj.weaver.world.ByteBuddyWorld;
import io.gemini.core.util.PlaceholderHelper;


/**
 * An AspectJ {@link TypeWorld} scoped to an aspect application.
 *
 * @author   martin.liu
 */
public class AspectTypeWorld extends ByteBuddyWorld {

    private final AspectClassLoader aspectClassLoader;
    private final TypeWorldFactory typeWorldFactory;


    public AspectTypeWorld(AspectTypePool typePool, PlaceholderHelper placeholderHelper, 
            AspectClassLoader aspectClassLoader, TypeWorldFactory typeWordlFactory) {
        super(typePool, placeholderHelper);

        this.aspectClassLoader = aspectClassLoader;
        this.typeWorldFactory = typeWordlFactory;
    }


    protected AspectClassLoader getAspectClassLoader() {
        return aspectClassLoader;
    }


    protected TypeWorldFactory getTypeWorldFactory() {
        return typeWorldFactory;
    }


    /**
     * Resolves types by first consulting the aspect class loader's own classpath,
     * then falling back to the target class loader's type world.
     *
     * @author   martin.liu
     */
    public static class PerTargetClassLoader extends AspectTypeWorld {

        public PerTargetClassLoader(AspectTypePool typePool, PlaceholderHelper placeholderHelper,
                AspectClassLoader.PerTargetClassLoader aspectClassLoader, TypeWorldFactory typeWordlFactory) {
            super(typePool, placeholderHelper, aspectClassLoader, typeWordlFactory);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public ResolvedType resolve(UnresolvedType unresolvedType, boolean allowMissing) {
            // 1.resolve target type firstly
            if (!unresolvedType.isPrimitiveType() && !(unresolvedType instanceof UnresolvedAspectType)) {
                try {
                    ClassLoader targetCL = getAspectClassLoader().getTargetClassLoader();
                    TypeWorld targetTypeWorld = getTypeWorldFactory().createTypeWorld(targetCL, null);

                    ResolvedType resolvedType = targetTypeWorld.getWorld().resolve(unresolvedType, allowMissing);
                    if (resolvedType != null && !resolvedType.isMissing())
                        return resolvedType;
                } catch (Exception ignored) { /* do nothing */ }
            }

            // 2.resolve aspect type
            return super.resolve(unresolvedType, allowMissing);
        }

        /** 
         * {@inheritDoc}
         */
        @Override
        protected AspectClassLoader.PerTargetClassLoader getAspectClassLoader() {
            return (AspectClassLoader.PerTargetClassLoader) super.getAspectClassLoader();
        }
    }

}
