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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import io.gemini.api.classloader.BaseClassLoader;
import io.gemini.core.concurrent.ConcurrentReferenceHashMap;
import io.gemini.core.util.Assert;
import io.gemini.core.util.ClassLoaderUtils;
import io.gemini.core.util.CompoundEnumeration;
import io.gemini.core.util.IOUtils;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * {@link AspectClassLoader} loads aspect application classes and resources.
 *
 * @author   martin.liu
 */
public class AspectClassLoader extends BaseClassLoader {

    private final String loaderName;

    private final Map<String, byte[]> typeDefnitionMap;


    static {
        // load class in parallel
        ClassLoader.registerAsParallelCapable();
    }


    /**
     * Create AspectClassLoader instance.
     * 
     * @param loaderName        class loader name
     * @param urls              class paths
     * @param parentClassLoader    parent class loader
     */
    public AspectClassLoader(String loaderName, URL[] urls, ClassLoader parentClassLoader) {
        super(urls, parentClassLoader);

        Assert.hasText(loaderName, "'loaderName' must not be empty.");
        this.loaderName = loaderName;

        this.typeDefnitionMap = new ConcurrentReferenceHashMap<>();
    }

    protected String getLoaderName() {
        return loaderName;
    }


    protected byte[] loadTypeDefinition(String name) throws IOException {
        return typeDefnitionMap.computeIfAbsent(
                name, 
                key -> {
                    String path = name.replace('.', '/').concat(".class");
                    InputStream inputStream = null;
                    try {
                        URL resource = findResource(path);
                        inputStream = resource.openStream();

                        return IOUtils.toByteArray( inputStream );
                    } catch (IOException e) {
                        return new byte[0];
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }
        );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return loaderName;
    }


    /**
     * This class loader loads classes and resources following below priority,
     * <ol>
     *   <li>aspect-side and target relevant: load from current class loader first (child-first), </li>
     *   <li>aspect-side and target irrelevant: load from parent class loader, </li>
     *   <li>target-side: </li>load from target class loader </li>
     * </ol>
     * By default, aspect-side classes are loaded first; target-side classes are loaded as a fallback.
     * The {@code targetTypeMatcher} and {@code targetResourceMatcher} can bypass this
     * priority for specific type or resource names.
     * </p>
     *
     * @author   martin.liu
     */
    public static class PerTargetClassLoader extends AspectClassLoader {

        private final ClassLoader targetClassLoader;

        private ElementMatcher<String> targetTypeMatcher = ElementMatchers.none();
        private ElementMatcher<String> targetResourceMatcher = ElementMatchers.none();

        private ElementMatcher<String> aspectTypeMatcher = ElementMatchers.none();


        static {
            // load class in parallel
            ClassLoader.registerAsParallelCapable();
        }

        /**
         * @param loaderName
         * @param urls
         * @param aspectClassLoader
         */
        public PerTargetClassLoader(String loaderName, AspectClassLoader aspectClassLoader,
                ClassLoader targetClassLoader) {
            super(loaderName, new URL[0], aspectClassLoader);

            this.targetClassLoader = targetClassLoader;
        }


        public void setTargetTypeMatcher(ElementMatcher<String> targetTypeMatcher) {
            this.targetTypeMatcher = targetTypeMatcher;
        }

        public void setTargetResourceMatcher(ElementMatcher<String> targetResourceMatcher) {
            this.targetResourceMatcher = targetResourceMatcher;
        }

        public void setAspectTypeMatcher(ElementMatcher<String> aspectTypeMatcher) {
            this.aspectTypeMatcher = aspectTypeMatcher;
        }


        public ClassLoader getTargetClassLoader() {
            return targetClassLoader;
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Assert.notNull(name, "'name' must not be empty.");

            synchronized (super.getClassLoadingLock(name)) {
                // 1.check local cache
                Class<?> type = this.findLoadedClass(name);
                if (type != null)
                    return type;


                // 2.load classes
                if (!this.targetTypeMatcher.matches(name)) {
                    // 2.1.load aspect classes via current class load which might depend on target classes (child-first)
                    if (this.aspectTypeMatcher.matches(name)) {
                        type = this.findClass(name);
                        if (type != null) {
                            if (resolve) {
                                resolveClass(type);
                            }
                            return type;
                        }
                    }

                    // 2.2.load AOP framework classes or aspect application classes shared across multiple AspectClassLoader.PerTargetClassLoader
                    type = loadClass(getParent(), name, resolve, false);
                    if (type != null)
                        return type;


                    // 2.3.load target classes via target class loader
                    return loadClass(getTargetClassLoader(), name, resolve, true);
                } else {
                    // 2.1.load target classes via target class loader for target-first classes
                    return loadClass(getTargetClassLoader(), name, resolve, true);
                }
            }
        }

        private Class<?> loadClass(ClassLoader classLoader, String name, boolean resolve, 
                boolean throwException) throws ClassNotFoundException {
            Class<?> type = null;
            try {
                type = classLoader.loadClass(name);
                if (resolve) {
                    resolveClass(type);
                }
            } catch (ClassNotFoundException e) {
                if (throwException) throw e;
            }

            return type;
        }

        /**
         * 
         * {@inheritDoc}
         */
        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            try {
                byte[] typeDefinition = ((AspectClassLoader) getParent()).loadTypeDefinition(name);
                if (typeDefinition.length == 0)
                    throw new ClassNotFoundException(name);

                return defineClass(name, typeDefinition, 0, typeDefinition.length );
            } catch (ClassNotFoundException  e) {
                throw e;
            } catch (Exception e) {
                throw new ClassNotFoundException(name, e);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public URL getResource(String name) {
            Assert.notNull(name, "'name' must not be empty.");

            ClassLoader targetCL = getTargetClassLoader();
            if (targetCL == null || !this.targetResourceMatcher.matches(name)) {
                // 1.1.load aspect and AOP framework resources via current and parent class loader
                URL url = super.getResource(name);
                if (url != null) {
                    return url;
                }

                // 1.2.load target resources via target class loader
                return targetCL != null ? targetCL.getResource(name) : null;
            } else {
                // 1.1.load target resources via target class loader
                return targetCL.getResource(name);

            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            List<Enumeration<URL>> urlsList = new ArrayList<>();

            ClassLoader targetCL = getTargetClassLoader();
            if (targetCL == null || !this.targetResourceMatcher.matches(name)) {
                // 1.1.load aspect and AOP framework resources via current and parent class loader
                Enumeration<URL> urls = super.getResources(name);
                if (urls != null) {
                    urlsList.add(urls);
                }

                // 1.2.load target resources via target class loader
                urls = targetCL != null ? targetCL.getResources(name) : null;
                if (urls != null) {
                    urlsList.add(urls);
                }

                return new CompoundEnumeration<>( urlsList );
            } else {
                // 1.1.load target resources via target class loader
                return targetCL.getResources(name);
            }
        }


        /** 
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return getLoaderName() + "-" + ClassLoaderUtils.getClassLoaderName(this.getTargetClassLoader());
        }
    }
}