package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx.build;

import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import javax.security.auth.Subject;

public class JMXSubjectProvider {

    private static final ConcurrentMutableMap<String, Subject> SUBJECTS = ConcurrentHashMap.newMap();

    private JMXSubjectProvider() {
        throw new UnsupportedOperationException("Static provider class");
    }

    public static void bindSubject(final String name,
                                   final Subject subject) {
        SUBJECTS.compute(
                name,
                (final String key, final Subject value) -> {
                    if (value == null) {
                        return subject;
                    }
                    throw new IllegalArgumentException("Subject already bound for name: " + name);
                }
        );
    }

    public static Subject getSubject(final String name) {
        return SUBJECTS.get(name);
    }

}
