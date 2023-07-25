package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.jayway.jsonpath.internal.function.PathFunction;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFunctionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathFunctionProvider.class);
    static final MutableMap<String, Class<?>> FUNCTIONS = Maps.mutable.of();

    public static void bindFunction(final String functionName,
                                    final Class<? extends PathFunction> function) {
        final Class<?> prevFunction = FUNCTIONS.put(functionName, function);
        if (prevFunction != null) {
            LOGGER.warn(
                    "Overwritten previous json-path function registration [Previous: {}] [New: {}]",
                    prevFunction.getName(),
                    function.getName()
            );
        }
    }

}
