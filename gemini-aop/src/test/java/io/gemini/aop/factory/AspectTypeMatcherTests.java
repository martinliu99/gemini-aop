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
package io.gemini.aop.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.gemini.aop.factory.DefaultAdvisorFactory.AspectTypeMatcherCreator;
import io.gemini.core.object.ClassScanner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;

public class AspectTypeMatcherTests {

    private final ClassLoader classLoader = this.getClass().getClassLoader();
    private ClassScanner classScanner;
    private Collection<String> adviceTypeNames;

    @BeforeEach
    public void setup() throws MalformedURLException {
        classScanner = new ClassScanner.Builder()
                .scannedClassLoaders(classLoader)
                .acceptPackages("io.gemini")
                .acceptJarPatterns("*.jar", "classes")
                .filteredClasspathElementUrls( getClassPathUrls() )
                .build();

        adviceTypeNames = new ArrayList<>();
        adviceTypeNames.add("io.gemini.aop.factory.redundant.aspects.ChildAspect");
        adviceTypeNames.add("io.gemini.aop.factory.conflict.aspecttype.Aspect");
        adviceTypeNames.add("io.gemini.aop.factory.conflict.aspectpackage.Aspect");
    }

    @Test
    public void test_createAspectTypeMatcher() {
        AspectTypeMatcherCreator creator = new AspectTypeMatcherCreator(
                classScanner, 
                TypePool.Default.of(classLoader),
                adviceTypeNames);
        ElementMatcher<String> matcher = creator.createAspectTypeMatcher();

        // 1.redundant cases
        // parentFirstType
        String redundantParentFirstPackage = "io.gemini.aop.factory.redundant.parent.";
        String redundantParentFirstType = redundantParentFirstPackage + "ParentObject";
        assertThat(creator.getParentFirstTypes()).doesNotContain(
                redundantParentFirstType
        );
        assertThat(creator.getParentFirstPackages()).contains(
                redundantParentFirstPackage
        );
        assertThat(matcher.matches(redundantParentFirstType)).isFalse();


        // childFirstType
        String redundantChildFirstPackage = "io.gemini.aop.factory.redundant.child.";
        String redundantChildFirstType = redundantChildFirstPackage + "ChildObject";
        assertThat(creator.getChildFirstTypes()).doesNotContain(
                redundantChildFirstType
        );
        assertThat(creator.getChildFirstPackages()).contains(
                redundantChildFirstPackage
        );
        assertThat(matcher.matches(redundantChildFirstType)).isTrue();


        // childFirstAspect
        String redundantChildFirstAspectPackage = "io.gemini.aop.factory.redundant.aspects.";
        String redundantChildFirstAspectType = redundantChildFirstAspectPackage + "ChildAspect";
        assertThat(creator.getChildFirstTypes()).doesNotContain(
                redundantChildFirstAspectType
        );
        assertThat(creator.getChildFirstPackages()).contains(
                redundantChildFirstAspectPackage
        );
        assertThat(matcher.matches(redundantChildFirstAspectType)).isTrue();
        assertThat(matcher.matches("io.gemini.aop.factory.redundant.aspects.ChildObject")).isTrue();


        // 2.conflict cases
        // conflict AspectType
        String conflictParentFirstAspectPackage = "io.gemini.aop.factory.conflict.aspecttype.";
        String conflictParentFirstAspectType = conflictParentFirstAspectPackage + "Aspect";
        assertThat(creator.getParentFirstTypes()).doesNotContain(
                conflictParentFirstAspectType
        );
        assertThat(creator.getChildFirstPackages()).doesNotContain(
                conflictParentFirstAspectPackage
        );
        assertThat(matcher.matches(conflictParentFirstAspectType)).isFalse();
        assertThat(matcher.matches(conflictParentFirstAspectPackage + "TargetObject")).isFalse();


        // conflict AspectPackage
        String conflictParentFirstPkgPackage = "io.gemini.aop.factory.conflict.aspectPackage.";
        String conflictParentFirstPkgType = conflictParentFirstPkgPackage + "Aspect";
        assertThat(creator.getChildFirstPackages()).doesNotContain(
                conflictParentFirstPkgPackage
        );
        assertThat(matcher.matches(conflictParentFirstPkgType)).isFalse();
        assertThat(matcher.matches(conflictParentFirstPkgPackage + "TargetObject")).isFalse();
    }

    public static URL[] getClassPathUrls() throws MalformedURLException {
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator, -1);
        URL[] urls = new URL[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = Paths.get(entries[i]).toAbsolutePath().toUri().toURL();
        }
        return urls;
    }
}
