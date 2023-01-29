package com.engineersbox.conduit.pipeline;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class IngestionContext implements Closeable {

    private final Map<String, Object> attributes;
    private long timeout;

    public IngestionContext() {
        this(60_000L);
    }

    public IngestionContext(final long timeout) {
        this.attributes = new HashMap<>();
        this.timeout = timeout;
    }

    public void putAttribute(final String key,
                             final Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(final String key) {
        return this.attributes.get(key);
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public static IngestionContext defaultContext() {
        return new IngestionContext() {
            @Override
            public void close() throws IOException {

            }
        };
    }

}
