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
package io.gemini.aop.integration;

/**
 * Tests visibility of target type referred by advice method.
 *
 * @author   martin.liu
 */
public class Advice_11TargetVisibility_Object {

    public Object testTargetObjectVisibility() {
        Advice_11TargetVisibility_Object.TargetObjectVisibility_Object object = new Advice_11TargetVisibility_Object.TargetObjectVisibility_Object();
        object.targetObject();

        return object;
    }

    protected static class TargetObjectVisibility_Object {

        public long targetObject() {
            return 1l;
        }
    }


    protected static class ArgumentsVisibility_Object {

        public Argument_Object arguments(Argument_Object argument) {
            return argument;
        }

        protected static class Argument_Object {}
    }


    static class ReturnValueVisibility_Object {

        public ReturnValueVisibility_Object.ReturnValue_Object returnValue() {
            return new ReturnValueVisibility_Object.ReturnValue_Object();
        }

        protected static class ReturnValue_Object {}
    }


    protected static class ThrownExceptionVisibility_Object {

        private ThrownExceptionVisibility_Object.ExceptionA_Object cause;


        public ThrownExceptionVisibility_Object(ThrownExceptionVisibility_Object.ExceptionA_Object cause) {
            this.cause = cause;
        }

        public void thrownException() throws ThrownExceptionVisibility_Object.ExceptionA_Object {
            throw cause;
        }


        protected static class ExceptionA_Object extends Exception {

            private static final long serialVersionUID = 3890639293213333957L;

            public ExceptionA_Object(String message) {
                super(message);
            }
        }
    }


    protected static class ParameterizedReturnValueVisibility_Object {

        public ParameterizedReturnValueVisibility_Object.ReturnValue_Object parameterizedReturnValue() {
            return new ParameterizedReturnValueVisibility_Object.ReturnValue_Object();
        }

        protected static class ReturnValue_Object {}
    }


    protected static class ParameterizedExceptionTypeVisibility_Object {

        private ParameterizedExceptionTypeVisibility_Object.ExceptionA_Object cause;


        public ParameterizedExceptionTypeVisibility_Object(ParameterizedExceptionTypeVisibility_Object.ExceptionA_Object cause) {
            this.cause = cause;
        }

        public void parameterizedExceptionType() throws ParameterizedExceptionTypeVisibility_Object.ExceptionA_Object {
            throw cause;
        }


        static class ExceptionA_Object extends Exception {

            private static final long serialVersionUID = 3890639293213333957L;

            public ExceptionA_Object(String message) {
                super(message);
            }
        }
    }
}
