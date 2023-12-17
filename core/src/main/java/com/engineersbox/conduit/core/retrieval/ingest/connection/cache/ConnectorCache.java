package com.engineersbox.conduit.core.retrieval.ingest.connection.cache;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class ConnectorCache implements RemovalListener<String, Connector<?,?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorCache.class);

    private final String globalKey;
    private final Cache<String, Connector<?,?>> cache;

    public ConnectorCache(final String globalKey,
                          final boolean recordStats,
                          final int concurrencyLevel) {
        this.globalKey = globalKey;
        final CacheBuilder<String, Connector<?,?>> builder = CacheBuilder.newBuilder()
                .removalListener(this)
                .concurrencyLevel(concurrencyLevel);
        if (recordStats) {
            builder.recordStats();
        }
        this.cache = builder.build();
    }

    @Override
    public void onRemoval(final RemovalNotification<String, Connector<?,?>> notification) {
        LOGGER.trace(
                "[Cache: {}] Connector removed from cache [Evicted: {}] [Cause: {}]",
                this.globalKey,
                notification.wasEvicted(),
                notification.getCause().name()
        );
        final Connector<?,?> connector = notification.getValue();
        if (connector == null) {
            LOGGER.trace(
                    "[Cache: {}] Removed value was null, skipping connector closure",
                    this.globalKey
            );
            return;
        }
        try {
            notification.getValue().close();
            LOGGER.trace(
                    "[Cache: {}] Closed removed connector",
                    this.globalKey
            );
        } catch (final Exception e) {
            LOGGER.error(
                    "[Cache: " + this.globalKey + "] Unable to close cached connector",
                    e
            );
        }
    }

    public String getGlobalKey() {
        return this.globalKey;
    }
    public Connector<?,?> get(final String key,
                              final Callable<? extends Connector<?,?>> callable) {
        try {
            return this.cache.get(key, callable);
        } catch (final ExecutionException e) {
            LOGGER.error(
                    "[Cache: " + this.globalKey + "] Unable to retrieve connector from cache with key " + key,
                    e
            );
            return null;
        }
    }

    public void put(final String key,
                    final Connector<?,?> callable) {
        this.cache.put(key, callable);
    }

    public void cleanUp() {
        this.cache.cleanUp();
    }

}
