package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.config.ConduitConfig;
import com.engineersbox.conduit.core.processing.PollingCondition;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.connection.cache.ConnectorCache;
import com.engineersbox.conduit.core.retrieval.ingest.source.Source;
import com.engineersbox.conduit.core.retrieval.ingest.source.provider.SourceProvider;
import com.engineersbox.conduit.core.schema.Schema;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.util.Functional;
import com.engineersbox.conduit.core.util.threading.ScopedThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Ingester<T, R, E extends ConnectorConfiguration, C extends Connector<T, E>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ingester.class);

    private ConnectorCache cache = null;
    private final boolean recordCacheStats;
    private final int cacheConcurrency;
    private final SourceProvider<T, R> sourceProvider;
    private final Schema schema;
    private C connector;
    private Optional<String> cacheKey;
    private final PollingCondition pollingCondition;
    private final ScopedThreadLocal<R, PollingCondition> data;

    @SuppressWarnings("unchecked")
    public Ingester(final SourceProvider<T, R> sourceProvider,
                    final Schema schema,
                    final PollingCondition pollingCondition,
                    final ConduitConfig config) {
        this.sourceProvider = sourceProvider;
        this.schema = schema;
        this.connector = (C) schema.getConnector();
        this.cacheKey = Optional.empty();
        this.pollingCondition = pollingCondition;
        this.data = new ScopedThreadLocal<>(PollingCondition.ON_EXECUTE::equals);
        this.recordCacheStats = config.ingest.connector_cache.record_stats;
        this.cacheConcurrency = config.ingest.connector_cache.concurrency_level;
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
                    "Cache does not exist, creating it [Record Stats: {}] [Concurrency: {}]",
                    this.recordCacheStats,
                    this.cacheConcurrency
            );
            this.cache = new ConnectorCache(
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
        this.data.set(null, this.pollingCondition);
    }

    public void consumeSource(final IngestionContext context,
                              final Metric metric) throws Exception {
        configureConnector(context);
        final Source<T, R> source = this.sourceProvider.apply(Thread.currentThread().threadId());
        LOGGER.trace(
                "Consuming source {} from connector {}",
                source.name(),
                this.connector.name()
        );
        final Supplier<R> dataSupplier = Functional.uncheckedSupplier(() -> source.invoke(
                this.connector,
                this.schema,
                metric,
                context
        ));
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
            this.data.set(
                    dataSupplier.get(),
                    this.pollingCondition
            );
        } else {
            consumeSourceAsync(
                    dataSupplier,
                    timeout,
                    timeUnit,
                    source
            );
        }
    }

    private void consumeSourceAsync(final Supplier<R> dataSupplier,
                                    final long timeout,
                                    final TimeUnit timeUnit,
                                    final Source<T, R> source) throws ExecutionException, InterruptedException {
        LOGGER.trace(
                "Invoking source with timeout of {} {}",
                timeout,
                timeUnit.name()
        );
        final CompletableFuture<R> future = CompletableFuture.supplyAsync(dataSupplier)
                .orTimeout(
                        timeout,
                        timeUnit
                ).exceptionally((final Throwable throwable) -> {
                    final R defaultValue = source.defaultDataValue();
                    if (throwable instanceof TimeoutException) {
                        throwable.fillInStackTrace();
                        LOGGER.warn(
                                "Source invocation did not complete within timeout of {} {}, defaulting to provided value [{}], Exception: {}",
                                timeout,
                                timeUnit.name(),
                                defaultValue,
                                throwable
                        );
                    } else {
                        LOGGER.error(
                                "Exception occurred during Source invocation, defaulting to provided value [" + defaultValue + "]",
                                throwable
                        );
                    }
                    return defaultValue;
                });
        this.data.set(
                future.get(),
                this.pollingCondition
        );
    }

    public R getCurrent() {
        return this.data.get(this.pollingCondition);
    }

}
