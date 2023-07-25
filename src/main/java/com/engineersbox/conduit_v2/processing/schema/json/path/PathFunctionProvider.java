package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.jayway.jsonpath.internal.function.PathFunction;
import com.jayway.jsonpath.internal.function.PathFunctionFactory;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@SuppressWarnings("unchecked")
public class PathFunctionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathFunctionProvider.class);
    static final Map<String, Class<?>> FUNCTIONS; // = Maps.mutable.of();

    static {
        try {
            FUNCTIONS = (Map<String, Class<?>>) FieldUtils.readField(
                    PathFunctionFactory.FUNCTIONS,
                    "m",
                    true
            );
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Unable to obtain mutable inner reference to PathFunctionFactory.FUNCTIONS:", e);
        }
    }

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
