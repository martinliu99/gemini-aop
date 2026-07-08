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
package io.gemini.aop.factory.redundant.aspects;

import io.gemini.api.aop.Advice;
import io.gemini.api.aop.Joinpoint.MutableJoinpoint;
import io.gemini.api.aop.annotation.ClassLoadingOrder;
import io.gemini.api.aop.annotation.ClassLoadingOrder.Order;
import io.gemini.api.aop.annotation.ExprPointcut;

@ClassLoadingOrder(Order.CHILD_FIRST)
@ExprPointcut("execution(io.gemini.aop.factory.child.ChildObject.new(...))")
public class ChildAspect extends Advice.AbstractAfter<Object, Exception> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void after(MutableJoinpoint<Object, Exception> joinpoint) throws Throwable {
    }
}
