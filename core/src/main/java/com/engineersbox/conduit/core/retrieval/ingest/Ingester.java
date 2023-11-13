package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.connection.cache.ConnectorCache;
import com.engineersbox.conduit.core.retrieval.ingest.source.Source;
import com.engineersbox.conduit.core.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Ingester<T, E extends ConnectorConfiguration, C extends Connector<T, E>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ingester.class);

    private ConnectorCache cache = null;
    private final boolean recordCacheStats = false;
    private final int cacheConcurrency = 5; // TODO: Make this configurable
    private final Source<T> source;
    private C connector;
    private Optional<String> cacheKey;
    private T data = null;

    public Ingester(final Source<T> source,
                    final C connector) {
        this.source = source;
        this.connector = connector;
        this.cacheKey = Optional.empty();
    }

    public void setCacheKey(final Optional<String> cacheKey) {
        this.cacheKey = cacheKey;
    }

    @SuppressWarnings("unchecked")
    private void configureConnector(final IngestionContext context) {
        if (this.connector.cacheKey.isEmpty()) {
            LOGGER.trace("Connector has no cache key, skipping caching");
            return;
        } else {
            LOGGER.trace("Connector has cache key, retrieving existing cache if global key is present");
            this.cacheKey.ifPresent((final String key) -> this.cache = context.getConnectorCache(key));
        }
        if (this.cache == null){
            LOGGER.trace(
                    "Cache does not exist, creating it [Record Stats: {}] [Concurrecy: {}]",
                    this.recordCacheStats,
                    this.cacheConcurrency
            );
            this. cache = new ConnectorCache(
                    this.cacheKey.orElse(null),
                    this.recordCacheStats,
                    this.cacheConcurrency
            );
        }

        this.cacheKey.ifPresent((final String key) -> context.getConnectorCaches().computeIfAbsent(
                key,
                k -> {
                    LOGGER.trace("Setting global cache instance for key \"{}\"", key);
                    return this.cache;
                }
        ));
        LOGGER.trace("Retrieved connector from cache");
        this.connector = (C) this.cache.get(
                this.connector.cacheKey.get(),
                () -> this.connector
        );
    }

    public void clear() {
        this.data = null;
    }

    public void consumeSource(final IngestionContext context) throws Exception {
        configureConnector(context);
        LOGGER.trace(
                "Consuming source {} from connector {}",
                this.source.name(),
                this.connector.name()
        );
        final Supplier<T> dataSupplier = Functional.uncheckedSupplier(
                () -> this.source.invoke(this.connector, context)
        );
        final Supplier<IngestionContext> contextSupplier = () -> context;
        final Long timeout = Functional.checkedApply(
                contextSupplier,
                IngestionContext::getTimeout,
                -1L
        );
        final TimeUnit timeUnit = Functional.checkedApply(
                contextSupplier,
                IngestionContext::getTimeUnit
        );
        if (timeout < 0 || timeUnit == null) {
            LOGGER.trace(
                    "Timeout was negative or time unit was null, invoking source without timeout failsafe [Timeout: {}] [TimeUnit: {}]",
                    timeout,
                    Functional.checkedApply(
                            () -> timeUnit,
                            TimeUnit::name
                    )
            );
            this.data = dataSupplier.get();
        } else {
            consumeSourceAsync(
                    dataSupplier,
                    timeout,
                    timeUnit
            );
        }
    }

    private void consumeSourceAsync(final Supplier<T> dataSupplier,
                                    final long timeout,
                                    final TimeUnit timeUnit) throws ExecutionException, InterruptedException {
        LOGGER.trace(
                "Invoking source with timeout of {} {}",
                timeout,
                timeUnit.name()
        );
        this.data = CompletableFuture.supplyAsync(dataSupplier)
                .orTimeout(
                        timeout,
                        timeUnit
                ).exceptionally((final Throwable throwable) -> {
                    final T defaultValue = source.defaultDataValue();
                    if (throwable instanceof TimeoutException) {
                        LOGGER.warn(
                                "Source invocation did not complete within timeout of {} {}, defaulting to provided value [{}]",
                                timeout,
                                timeUnit.name(),
                                defaultValue
                        );
                    } else {
                        LOGGER.error(
                                "Exception occurred during Source invocation, defaulting to provided value [" + defaultValue + "]",
                                throwable
                        );
                    }
                    return defaultValue;
                }).get();
    }

    public T getCurrent() {
        return this.data;
    }

}
