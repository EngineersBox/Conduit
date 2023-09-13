package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.constraint;

import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.security.AlgorithmConstraints;

public class AlgorithmConstraintsProvider {

    private static final ConcurrentMutableMap<String, AlgorithmConstraints> CONSTRAINTS = ConcurrentHashMap.newMap();

    private AlgorithmConstraintsProvider() {
        throw new UnsupportedOperationException("Static provider class");
    }

    public static void bindConstraint(final String name,
                                      final AlgorithmConstraints constraints) {
        CONSTRAINTS.compute(
                name,
                (final String key, final AlgorithmConstraints value) -> {
                    if (value == null) {
                        return constraints;
                    }
                    throw new IllegalArgumentException("Algorithm constraint already bound for name: " + name);
                }
        );
    }

    public static AlgorithmConstraints getConstraint(final String name) {
        return CONSTRAINTS.get(name);
    }

}
