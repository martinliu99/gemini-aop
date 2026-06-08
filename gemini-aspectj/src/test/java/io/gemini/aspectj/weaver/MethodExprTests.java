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
package io.gemini.aspectj.weaver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.gemini.aspectj.weaver.world.BytebuddyWorld;
import net.bytebuddy.agent.builder.AgentBuilder.PoolStrategy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

public class MethodExprTests extends AbstractTests {

    @Test
    public void testFindMethod() {
        BytebuddyWorld typeWorld = this.doCreateTypeWorld();

        PoolStrategy poolStrategy = PoolStrategy.ClassLoading.FAST;
        TypePool typePool = poolStrategy.typePool(ClassFileLocator.NoOp.INSTANCE, this.getClass().getClassLoader());
        TypeDescription type = typePool.describe(MethodExprTests.class.getName()).resolve();

        String methodExpression = "public java.util.List io.gemini.aspectj.weaver.MethodExprTests.replace(java.lang.String, int, java.lang.String)";
        MethodDescription method = ExprParser.INSTANCE.findMethod(typeWorld, methodExpression);
        assertThat(method).isNotNull();
        assertThat(method).isEqualTo(
                        type.getDeclaredMethods().filter( ElementMatchers.named("replace") ).getOnly()
        );
    }


    public List<String> replace(String arg, int count, String t) {
        return null;
    }
}
