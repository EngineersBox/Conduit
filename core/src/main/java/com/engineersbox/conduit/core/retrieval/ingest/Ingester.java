package com.engineersbox.conduit.core.retrieval.ingest;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.util.Functional;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class Ingester<T, E extends ConnectorConfiguration, C extends Connector<T, E>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ingester.class);

    private final Source<T> source;
    private final C connector;
    private T data = null;

    public Ingester(final Source<T> source,
                    final C connector) {
        this.source = source;
        this.connector = connector;
    }

    public void clear() {
        this.data = null;
    }

    public void consumeSource(final IngestionContext context) throws Exception {
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
