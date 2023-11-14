package com.engineersbox.conduit.core.retrieval.ingest.source.method;

import com.engineersbox.conduit.core.schema.json.path.PathFunctionProvider;
import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.PathRef;
import com.jayway.jsonpath.internal.function.Parameter;
import com.jayway.jsonpath.internal.function.PathFunction;

import javax.management.*;
import java.io.IOException;
import java.util.List;

public class JMXMBeanInvoke implements PathFunction {

    public static final String FUNCTION_NAME = "jmxMBean";

    @Override
    public Object invoke(final String currentPath,
                         final PathRef pathRef,
                         final Object model,
                         final EvaluationContext evaluationContext,
                         final List<Parameter> parameters) {
        if (!(model instanceof MBeanServerConnection connection)) {
            throw new IllegalArgumentException("Cannot use \"" + FUNCTION_NAME + "\" on non-JMX model (Source<T> data)");
        }
        if (parameters.size() != 2) {
            throw new IllegalArgumentException(
                    "Expected function format: jmxMBean(\"<ObjectName>\",\"<Method>\")"
            );
        }
        final Object objectNameRaw = parameters.get(0);
        if (!(objectNameRaw instanceof String objectName)) {
            throw new IllegalArgumentException("Expected string object name, got " + objectNameRaw.getClass());
        }
        final Object methodNameRaw = parameters.get(1);
        if (!(methodNameRaw instanceof String methodName)) {
            throw new IllegalArgumentException("Expected string method name, got " + methodNameRaw.getClass());
        }
        try {
            return connection.invoke(
                    new ObjectName(objectName),
                    methodName,
                    new Object[0], // NOTE: Should we expose all of these via path?
                    new String[0]
            );
        } catch (final InstanceNotFoundException
                 | MalformedObjectNameException
                 | IOException
                 | ReflectionException
                 | MBeanException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void register() {
        PathFunctionProvider.bindFunction(
                FUNCTION_NAME,
                JMXMBeanInvoke.class
        );
    }

}
