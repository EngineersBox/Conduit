package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.cache.ConnectorCache;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IngestionContext {

    private final Map<String, Object> globalAttributes;
    private final Map<Long, Map<String, Object>> threadAttributes;
    private final Map<String, ConnectorCache> connectorCaches;
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
                Maps.mutable.empty(),
                Maps.mutable.empty()
        );
    }

    public IngestionContext(final long timeout,
                            final TimeUnit timeUnit,
                            final Map<String, Object> globalAttributes,
                            final Map<Long, Map<String, Object>> threadAttributes,
                            final Map<String, ConnectorCache> connectorCaches) {
        this.globalAttributes = globalAttributes;
        this.threadAttributes = threadAttributes;
        this.connectorCaches = connectorCaches;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * Set a thread specific attribute (keyed on the current thread from {@code Thread.currentThread().threadId()})
     * @param key Attribute key
     * @param value Attribute value
     */
    public IngestionContext putAttribute(final String key,
                             final Object value) {
        return putAttribute(
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
    public IngestionContext putAttribute(final long threadId,
                             final String key,
                             final Object value) {
        this.threadAttributes.computeIfAbsent(
                threadId,
                (_map) -> Maps.mutable.empty()
        ).put(key, value);
        return this;
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
    public IngestionContext putGlobalAttribute(final String key,
                                   final Object value) {
        this.globalAttributes.put(key, value);
        return this;
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

    public IngestionContext setTimeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public IngestionContext setTimeUnit(final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public ConnectorCache getConnectorCache(final String cacheKey) {
        return this.connectorCaches.get(cacheKey);
    }

    public IngestionContext putConnectorCache(final String key,
                                              final ConnectorCache connectorCache) {
        this.connectorCaches.put(key, connectorCache);
        return this;
    }

    public Map<String, ConnectorCache> getConnectorCaches() {
        return this.connectorCaches;
    }
    public static IngestionContext defaultContext() {
        return new IngestionContext() {};
    }

}
