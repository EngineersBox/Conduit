package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.jayway.jsonpath.internal.function.PathFunction;

public aspect PathFunctionRegistrationAspect {

    pointcut pathFuncInstantiationVisit(final String functionName):
            execution(public static com.jayway.jsonpath.internal.function.PathFunction com.jayway.jsonpath.internal.function.PathFunctionFactory.newFunction(String))
            && args(functionName);

    PathFunction around(String functionName): pathFuncInstantiationVisit(functionName) {
        final PathFunction function = PathFunctionProvider.getFunctionInstance(functionName);
        if (function != null) {
            return function;
        }
        return proceed(functionName);
    }
    // -javaagent:$MAVEN_REPOSITORY$/org/aspectj/aspectjweaver/1.9.19/aspectjweaver-1.9.19.jar
}
