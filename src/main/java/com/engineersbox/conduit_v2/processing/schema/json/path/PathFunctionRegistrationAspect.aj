package com.engineersbox.conduit_v2.processing.schema.json.path;

public aspect PathFunctionRegistrationAspect {

    pointcut pathFuncInstantiationVisit():
            execution(* com.jayway.jsonpath.internal.function.PathFunctionFactory.newFunction(String));

    around (): pathFuncInstantiation() {
        
    }

}
