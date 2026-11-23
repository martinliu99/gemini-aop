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

import io.gemini.aop.test.ExecutionMemento;
import io.gemini.aop.test.ExecutionMemento.TargetMethod;

/** 
 * Tests visibility of target type referred by advice method.
 *
 * @author   martin.liu
 */
public class Advice_11TargetObject_Object {


    public static abstract class TargetBase_Object {
        
    }


    public static class TargetObject_Object extends TargetBase_Object {

        public static final String TARGET_METHOD = TargetObject_Object.class.getName() + ".targetObject";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Target_11TargetObject_Object$TargetObject_Object.targetObject(..))";


        public void targetObject() {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withTargetObject(this) );
        }
    }

    public static class Argument_Object extends TargetBase_Object {

        public static final String TARGET_METHOD = Argument_Object.class.getName() + ".arguments";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Target_11TargetObject_Object$Argument_Object.arguments(..))";


        public void arguments(Argument_Object arg, long arg2, Long arg3) {
            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withArgumnts(arg, arg2, arg3) );
        }
    }

    public static class ReturnValue_Object extends TargetBase_Object {

        public static final String TARGET_METHOD = ReturnValue_Object.class.getName() + ".returnValue";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Target_11TargetObject_Object$ReturnValue_Object.returnValue(..))";


        public ReturnValue_Object returnValue() {
            ReturnValue_Object returnValue = new ReturnValue_Object();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withReturnValue(returnValue) );

            return returnValue;
        }
    }

    public static class ThrownException_Object extends Exception {

        private static final long serialVersionUID = -8287385160059501926L;

        public static final String TARGET_METHOD = ThrownException_Object.class.getName() + ".thrownException";

        public static final String POINTCUT_EXPRESSION = 
                "execution(public * io.gemini.aop.integration.Target_11TargetObject_Object$ThrownException_Object.thrownException(..))";


        public void thrownException() throws ThrownException_Object {
            ThrownException_Object thrownException = new ThrownException_Object();

            ExecutionMemento.putTargetMethodInvoker(TARGET_METHOD, 
                    new TargetMethod()
                        .withThrownException(thrownException) );

            throw thrownException;
        }
    }


    public static class TargetSub_Object extends TargetObject_Object {
        
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
