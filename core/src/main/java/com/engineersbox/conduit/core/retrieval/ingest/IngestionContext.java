package com.engineersbox.conduit.core.retrieval.ingest;

import java.util.HashMap;
import java.util.Map;

public class IngestionContext {

    private final Map<String, Object> attributes;
    private long timeout;

    private IngestionContext() {
        this(60_000L);
    }

    public IngestionContext(final long timeout) {
        this(
                timeout,
                new HashMap<>()
        );
    }

    public IngestionContext(final long timeout, final Map<String, Object> attributes) {
        this.attributes = attributes;
        this.timeout = timeout;
    }

    public void putAttribute(final String key,
                             final Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(final String key) {
        return this.attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public static IngestionContext defaultContext() {
        return new IngestionContext() {};
    }

}
