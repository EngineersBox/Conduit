package com.engineersbox.conduit.core.retrieval.ingest.source.method;

import com.jayway.jsonpath.internal.EvaluationContext;
import com.jayway.jsonpath.internal.PathRef;
import com.jayway.jsonpath.internal.function.Parameter;
import com.jayway.jsonpath.internal.function.PathFunction;

import java.util.List;

public class JMXInvoke implements PathFunction {
    @Override
    public Object invoke(final String currentPath,
                         final PathRef pathRef,
                         final Object model,
                         final EvaluationContext evaluationContext,
                         final List<Parameter> parameters) {
        return null;
    }

    public static void register() {

    }

}
