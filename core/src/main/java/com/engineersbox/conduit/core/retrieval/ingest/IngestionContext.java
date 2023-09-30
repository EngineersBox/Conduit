package com.engineersbox.conduit.core.retrieval.ingest;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IngestionContext {

    private final Map<String, Object> globalAttributes;
    private final Map<Long, Map<String, Object>> threadAttributes;
    private long timeout;
    private TimeUnit timeUnit;

    private IngestionContext() {
        this(
                10_000L,
                TimeUnit.MILLISECONDS
        );
    }

    public IngestionContext(final long timeout,
                            final TimeUnit timeUnit) {
        this(
                timeout,
                timeUnit,
                new ConcurrentHashMap<>(),
                Maps.mutable.empty()
        );
    }

    public IngestionContext(final long timeout,
                            final TimeUnit timeUnit,
                            final Map<String, Object> globalAttributes,
                            final Map<Long, Map<String, Object>> threadAttributes) {
        this.globalAttributes = globalAttributes;
        this.threadAttributes = threadAttributes;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * Set a thread specific attribute (keyed on the current thread from {@code Thread.currentThread().threadId()})
     * @param key Attribute key
     * @param value Attribute value
     */
    public void putAttribute(final String key,
                             final Object value) {
        putAttribute(
                Thread.currentThread().threadId(),
                key,
                value
        );
    }

    /**
     * Set a thread specific attribute (provided)
     * @param threadId Thread id
     * @param key Attribute key
     * @param value Attribute value
     */
    public void putAttribute(final long threadId,
                             final String key,
                             final Object value) {
        this.threadAttributes.computeIfAbsent(
                threadId,
                (_map) -> Maps.mutable.empty()
        ).put(key, value);
    }

    /**
     * Get a thread specific attribute (keyed on the current thread from {@code Thread.currentThread().threadId()})
     * @param key Attribute key
     * @return Attribute value or null if not found
     */
    public Object getAttribute(final String key) {
        return getAttribute(
                Thread.currentThread().threadId(),
                key
        );
    }

    /**
     * Get a thread specific attribute (provided)
     * @param threadId Thread id
     * @param key Attribute key
     * @return Attribute value or null if not found
     */
    public Object getAttribute(final long threadId,
                               final String key) {
        final Map<String, Object> threadMap = this.threadAttributes.get(threadId);
        if (threadMap == null) {
            return null;
        }
        return threadMap.get(key);
    }

    /**
     * Get the attribute map for a specific thread (keyed on the current thread from {@code Thread.currentThread().threadId()})
     * @return Thread attribute map or null if not found
     */
    public Map<String, Object> getAttributes() {
        return getAttributes(Thread.currentThread().threadId());
    }

    /**
     * Get the attribute map for a specific thread (provided)
     * @param threadId Thread id
     * @return Thread attribute map or null if not found
     */
    public Map<String, Object> getAttributes(final long threadId) {
        return this.threadAttributes.get(threadId);
    }

    /**
     * Set a global attribute
     * @param key Attribute key
     * @param value Attribute value
     */
    public void putGlobalAttribute(final String key,
                                   final Object value) {
        this.globalAttributes.put(key, value);
    }

    /**
     * Get a global attribute
     * @param key Attribute key
     * @return Attribute value or null if not found
     */
    public Object getGlobalAttribute(final String key) {
        return this.globalAttributes.get(key);
    }

    public Map<String, Object> getGlobalAttributes() {
        return this.globalAttributes;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeUnit(final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public static IngestionContext defaultContext() {
        return new IngestionContext() {};
    }

}
