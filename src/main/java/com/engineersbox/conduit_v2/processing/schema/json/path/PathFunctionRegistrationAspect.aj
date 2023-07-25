package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.jayway.jsonpath.internal.function.PathFunction;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;

public aspect PathFunctionRegistrationAspect {

    pointcut pathFuncInstantiationVisit(final String functionName):
            execution(public static com.jayway.jsonpath.internal.function.PathFunction com.jayway.jsonpath.internal.function.PathFunctionFactory.newFunction(String))
            && args(functionName);

    PathFunction around(String functionName): pathFuncInstantiationVisit(functionName) {
        System.out.println("INVOKING LTW ASPECT FOR PATH FUNCTIONS");
        final Class<?> custom = PathFunctionProvider.FUNCTIONS.get(functionName);
        if (custom != null) {
            try {
                return (PathFunction) ConstructorUtils.invokeConstructor(custom);
            } catch (final NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new RuntimeException("Cannot instantiate custom path function: " + functionName, e);
            }
        }
        return proceed(functionName);
    }
    // -javaagent:$MAVEN_REPOSITORY$/org/aspectj/aspectjweaver/1.9.19/aspectjweaver-1.9.19.jar
}
