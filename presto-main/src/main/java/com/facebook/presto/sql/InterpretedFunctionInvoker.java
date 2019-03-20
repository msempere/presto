/*
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
package com.facebook.presto.sql;

import com.facebook.presto.metadata.FunctionManager;
import com.facebook.presto.operator.scalar.ScalarFunctionImplementation;
import com.facebook.presto.operator.scalar.ScalarFunctionImplementation.ArgumentProperty;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.function.FunctionHandle;
import com.facebook.presto.spi.function.Signature;
import com.google.common.base.Defaults;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.facebook.presto.operator.scalar.ScalarFunctionImplementation.ArgumentType.VALUE_TYPE;
import static com.facebook.presto.operator.scalar.ScalarFunctionImplementation.NullConvention.USE_NULL_FLAG;
import static com.google.common.base.Throwables.throwIfUnchecked;
import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;
import static java.util.Objects.requireNonNull;

public class InterpretedFunctionInvoker
{
    private final FunctionManager functionManager;

    public InterpretedFunctionInvoker(FunctionManager functionManager)
    {
        this.functionManager = requireNonNull(functionManager, "registry is null");
    }

    public Object invoke(FunctionHandle functionHandle, ConnectorSession session, Object... arguments)
    {
        return invoke(functionHandle, session, Arrays.asList(arguments));
    }

    public Object invoke(FunctionHandle functionHandle, ConnectorSession session, List<Object> arguments)
    {
        return invoke(functionManager.getScalarFunctionImplementation(functionHandle), session, arguments);
    }

    public Object invoke(Signature function, ConnectorSession session, List<Object> arguments)
    {
        return invoke(functionManager.getScalarFunctionImplementation(function), session, arguments);
    }

    /**
     * Arguments must be the native container type for the corresponding SQL types.
     * <p>
     * Returns a value in the native container type corresponding to the declared SQL return type
     */
    private Object invoke(ScalarFunctionImplementation function, ConnectorSession session, List<Object> arguments)
    {
        MethodHandle method = function.getMethodHandle();

        // handle function on instance method, to allow use of fields
        method = bindInstanceFactory(method, function);

        if (method.type().parameterCount() > 0 && method.type().parameterType(0) == ConnectorSession.class) {
            method = method.bindTo(session);
        }
        List<Object> actualArguments = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            Object argument = arguments.get(i);
            ArgumentProperty argumentProperty = function.getArgumentProperty(i);
            if (argumentProperty.getArgumentType() == VALUE_TYPE) {
                if (function.getArgumentProperty(i).getNullConvention() == USE_NULL_FLAG) {
                    boolean isNull = argument == null;
                    if (isNull) {
                        argument = Defaults.defaultValue(method.type().parameterType(actualArguments.size()));
                    }
                    actualArguments.add(argument);
                    actualArguments.add(isNull);
                }
                else {
                    actualArguments.add(argument);
                }
            }
            else {
                argument = asInterfaceInstance(argumentProperty.getLambdaInterface(), (MethodHandle) argument);
                actualArguments.add(argument);
            }
        }

        try {
            return method.invokeWithArguments(actualArguments);
        }
        catch (Throwable throwable) {
            throw propagate(throwable);
        }
    }

    private static MethodHandle bindInstanceFactory(MethodHandle method, ScalarFunctionImplementation implementation)
    {
        if (!implementation.getInstanceFactory().isPresent()) {
            return method;
        }

        try {
            return method.bindTo(implementation.getInstanceFactory().get().invoke());
        }
        catch (Throwable throwable) {
            throw propagate(throwable);
        }
    }

    private static RuntimeException propagate(Throwable throwable)
    {
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        throwIfUnchecked(throwable);
        throw new RuntimeException(throwable);
    }
}