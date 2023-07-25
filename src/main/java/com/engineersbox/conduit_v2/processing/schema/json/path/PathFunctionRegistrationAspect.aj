package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.jayway.jsonpath.internal.function.PathFunction;

import java.util.Objects;

public aspect PathFunctionRegistrationAspect {

    pointcut pathFuncInstantiationVisit(final String functionName):
            execution(public static com.jayway.jsonpath.internal.function.PathFunction com.jayway.jsonpath.internal.function.PathFunctionFactory.newFunction(String))
            && args(functionName);

    PathFunction around(String functionName): pathFuncInstantiationVisit(functionName) {
        System.out.println("INVOKING LTW ASPECT FOR PATH FUNCTIONS");
        final PathFunction function = PathFunctionProvider.getFunctionInstance(functionName);
        return Objects.requireNonNullElse(function, proceed(functionName));
    }
    // -javaagent:$MAVEN_REPOSITORY$/org/aspectj/aspectjweaver/1.9.19/aspectjweaver-1.9.19.jar
}
