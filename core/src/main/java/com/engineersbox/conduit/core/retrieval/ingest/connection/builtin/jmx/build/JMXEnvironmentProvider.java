package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx.build;

import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.Map;

public class JMXEnvironmentProvider {

    private static final ConcurrentMutableMap<String, Map<String, ?>> ENVIRONMENTS = ConcurrentHashMap.newMap();

    private JMXEnvironmentProvider() {
        throw new UnsupportedOperationException("Static provider class");
    }

    public static void bindEnvironment(final String name,
                                       final Map<String, ?> environment) {
        ENVIRONMENTS.compute(
                name,
                (final String key, final Map<String, ?> value) -> {
                    if (value == null) {
                        return environment;
                    }
                    throw new IllegalArgumentException("Subject already bound for name: " + name);
                }
        );
    }

    public static Map<String, ?> getEnvironment(final String name) {
        return ENVIRONMENTS.get(name);
    }

}
