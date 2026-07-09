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
package io.gemini.aop.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.gemini.activation.AopActivator;
import io.gemini.activation.classloader.DefaultAopClassLoader;
import io.gemini.activation.support.AspectAppScanner;
import io.gemini.activation.support.UnpackedArchiveConfig;
import io.gemini.api.activation.LauncherConfig;
import io.gemini.api.classloader.AopClassLoader;
import io.gemini.core.util.ClassLoaderUtils;
import io.gemini.core.util.ClassUtils;
import io.gemini.core.util.IOUtils;
import io.gemini.core.util.SingleEnumeration;
import net.bytebuddy.agent.ByteBuddyAgent;

/**
 * Activates the Gemini AOP framework before JUnit 5 test execution.
 * <p>
 * Registered as a JUnit 5 {@link org.junit.platform.launcher.LauncherDiscoveryListener}
 * and {@link org.junit.platform.launcher.TestExecutionListener} via the ServiceLoader mechanism.
 * On first test discovery, installs the ByteBuddy agent, builds the launcher configuration
 * from the test classpath, and calls {@link AopActivator#activateAop}.
 * </p>
 *
 * @author   martin.liu
 */
public class AopTestActivator implements LauncherDiscoveryListener, TestExecutionListener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AopTestActivator.class);

    private static final String AOP_DEPENDENCY_NAME = "gemini-aop";

    private static final String DEPENDENCIES_FILE_RELATIVE_PATH = "META-INF/dependencies.txt";
    private static final String TARGET_CLASSES_RELATIVE_PATH = "target/classes";
    private static final String TEST_CLASSES_RELATIVE_PATH = "target/test-classes";

    private static boolean LAUNCHED = false;


    /**
     * {@inheritDoc}
     */
    @Override
    public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {
        // launch AopLauncher and load all advisors
        if (LAUNCHED == false) {
            try {
                launch();

                LAUNCHED = true;
            } catch (Exception e) {
                LOGGER.warn("Could not activate AopTestActivator.", e);
            }
        }
    }

    protected void launch() throws Exception {
        // 1.prepare arguments
        Instrumentation instrumentation = ByteBuddyAgent.install();

        URL rootResource = AopTestActivator.class.getClassLoader().getResource("");
        String launchLocation = new File(rootResource.toURI()).getParentFile().getPath() + File.separator + "aop-int-test" + File.separator;
        Path launchPath = Paths.get(launchLocation);
        if (Files.exists(launchPath) == false)
            Files.createDirectory( launchPath);

        List<Path> classPaths = ClassLoaderUtils.getClassPaths();

        // load AOP framework dependencies
        List<URL> aopDependencies = resolveAopDependencies(classPaths);

        // collect resource files
        Map<String, URL> resourceFileURLs = new LinkedHashMap<>();
        for (Path classPath : classPaths) {
            collectResourceURLs(classPath, resourceFileURLs);
        }


        // 2.activate AopLauncher
        try {
            LauncherConfig launcherConfig = new UnpackedArchiveConfig(launchPath, null, "",
                    () -> aopDependencies.toArray( new URL[0]),
                    true,
                    new AspectAppScanner.ClassesFolder() );

            AopClassLoader aopClassLoader = new DefaultAopClassLoader(aopDependencies.toArray(new URL[0]),  AopTestActivator.class.getClassLoader());

            configureClassLoader(aopClassLoader, resourceFileURLs);

            AopActivator.activateAop(launchLocation, instrumentation, launcherConfig, aopClassLoader);
        } catch (Throwable t) {
            throw new IllegalStateException("Could not activate AopLauncher.", t);
        }
    }

    private List<URL> resolveAopDependencies(List<Path> classPaths) {
        // 1.load gemini-aop dependencies file
        Path aopDependencyPath = null;
        String dependenciesText = null;
        for (Path classPath : classPaths) {
            if (Files.isRegularFile(classPath)) {
                // load dependencies file from jar file
                String fileName = classPath.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!fileName.endsWith(".jar") || !fileName.startsWith(AOP_DEPENDENCY_NAME))
                    continue;

                aopDependencyPath = classPath;

                try (JarFile aopJarFile = new JarFile(classPath.toFile()) ){
                    ZipEntry aopJarEntry = aopJarFile.getEntry(DEPENDENCIES_FILE_RELATIVE_PATH);
                    if (aopJarEntry == null) {
                        LOGGER.warn("Could not find '{}' from jar file '{}'.", 
                                DEPENDENCIES_FILE_RELATIVE_PATH, classPath);
                        continue;
                    }

                    try (InputStream inputStream = aopJarFile.getInputStream(aopJarEntry)) {
                        dependenciesText = IOUtils.toString(inputStream);
                    }

                    break;
                } catch (IOException e) {
                    LOGGER.warn("Could not resolve '{}' from jar file '{}'.", 
                            DEPENDENCIES_FILE_RELATIVE_PATH, classPath, e);
                }
            } else {
                // load dependencies file under target-classes folder
                if (!classPath.endsWith(TARGET_CLASSES_RELATIVE_PATH) 
                        || !classPath.getParent().getParent().getFileName().toString().equals(AOP_DEPENDENCY_NAME))
                    continue;

                aopDependencyPath = classPath;

                Path aopClassesPath = classPath.resolve(DEPENDENCIES_FILE_RELATIVE_PATH);
                if (!Files.exists(aopClassesPath)) {
                    LOGGER.warn("Could not find '{}' under '{}'.", DEPENDENCIES_FILE_RELATIVE_PATH, aopClassesPath);
                    continue;
                }

                try (InputStream inputStream = new FileInputStream( aopClassesPath.toFile()) ) {
                    dependenciesText = IOUtils.toString(inputStream);

                    break;
                } catch (Exception e) {
                    LOGGER.warn("Could not find '{}' under '{}'.", DEPENDENCIES_FILE_RELATIVE_PATH, aopClassesPath, e);
                }
            }
        }
        if (dependenciesText == null)
            return new ArrayList<>();


        // 2.resolve gemini-aop dependencies
        String[] aopClassPathValues = dependenciesText.split("\n", -1);

        List<URL> aopClassPaths = new ArrayList<>(aopClassPathValues.length + 1);
        collectClassPath(aopDependencyPath, aopClassPaths);

        for (String aopClassPathValue : aopClassPathValues) {
            for (Path classPath : classPaths) {
                if (classPath.endsWith(aopClassPathValue)
                        || (classPath.endsWith(TARGET_CLASSES_RELATIVE_PATH) 
                                && aopClassPathValue.startsWith(classPath.getParent().getParent().getFileName().toString())) )
                    collectClassPath(classPath, aopClassPaths);
            }
        }

        return aopClassPaths;
    }

    private void collectResourceURLs(Path classPath, Map<String, URL> resourceFileURLs) {
        // iterate test-classes folder to load resource files
        if (!classPath.endsWith(TEST_CLASSES_RELATIVE_PATH)) 
            return ;

        try (Stream<Path> stream = Files.walk(classPath)) {
            stream
            .filter( Files::isRegularFile )
            .filter( p -> p.toString().endsWith(ClassUtils.CLASS_FILE_EXTENSION) == false )
            .forEach( p -> {
                try {
                    String path = classPath.relativize(p.normalize()).toString().replace('\\', ClassUtils.RESOURCE_SPERATOR);
                    resourceFileURLs.put(path, p.toUri().toURL());
                } catch (MalformedURLException e) {
                    LOGGER.warn("Could not convert path: {}", p, e);
                }
            } );
        } catch (IOException e) {
            LOGGER.warn("Could not iterate path: {}", classPath, e);
        }
    }

    private void collectClassPath(Path dependentClassPath, List<URL> aopClassPaths) {
        try {
            aopClassPaths.add(dependentClassPath.toUri().toURL());
        } catch (MalformedURLException e) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Could not fetch class path URL '{}'.", dependentClassPath, e);
        }
    }


    private void configureClassLoader(AopClassLoader aopClassLoader, Map<String, URL> resourceFileURLs) {
        // 1.load {@code ExecutionMemento} relevant classes via Launcher ClassLoader.
        aopClassLoader.addLauncherFirstFilter( new AopClassLoader.LauncherFirstFilter() {

            private final String launcherFirstClassPrefix;
            private final String launcherFirstResourcePrefix;


            {
                this.launcherFirstClassPrefix = ExecutionMemento.class.getName();
                this.launcherFirstResourcePrefix = ClassUtils.convertClassToResource(launcherFirstClassPrefix);
            }


            @Override
            public boolean isLauncherFirstClass(String name) {
                return name.startsWith(launcherFirstClassPrefix);
            }

            @Override
            public boolean isLauncherFirstResource(String name) {
                return name.startsWith(launcherFirstResourcePrefix);
            }
        } );


        // 2.load configuration files
        aopClassLoader.addTypeFinder( new AopClassLoader.TypeFinder() {

            @Override
            public byte[] findByteCode(String name) {
                return null;
            }

            @Override
            public URL findResource(String name) {
                return resourceFileURLs.get(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                URL resources = resourceFileURLs.get(name);
                return resources != null ? new SingleEnumeration<>(resources) : null;
            }
        } );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        ExecutionMemento.clearMemento();
    }
}
