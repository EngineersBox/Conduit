package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.processing.task.WaitableTaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider;
    private final WaitableTaskExecutorPool executor;
    private final ConduitConfig config;
    private boolean executing = false;
    private ContentManager<?, ?, ? ,?> contentManager;

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final Supplier<RiemannClient> clientProvider,
                   final String configPath) {
        this(
                schemaProvider,
                clientProvider,
                ConfigFactory.create(configPath)
        );
    }

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final Supplier<RiemannClient> clientProvider,
                   final ConduitConfig config) {
        this.schemaProvider = schemaProvider;
        // TODO: Implement the client provider
        final int parallelism = config.executor.task_pool_size.orElse(Runtime.getRuntime().availableProcessors());
        this.executor = new WaitableTaskExecutorPool(
                clientProvider,
                parallelism
        );
        this.config = config;
    }

    public void execute() {
        this.executing = true;
        final MetricsSchema schema = this.schemaProvider.provide();
        if (this.config.ingest.schema_provider_locking) {
            this.schemaProvider.lock();
        }
        if (this.schemaProvider.instanceRefreshed()) {
            this.contentManager = ContentManagerFactory.construct(
                    schema,
                    null,
                    Function.identity() // TODO: allow customisation via config
            );
        }
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(contentManager);
        LazyIterable<Metric> workload = Lists.immutable.<Metric>of().asLazy();
        // TODO: Update this when MetricSchema changed to use new Metric class or new metric class replaced with old one
        //       and values are accessible as Eclipse immutable collection
        // workload = schema.values().asLazy();

        this.contentManager.poll();
        final LazyIterable<RichIterable<Metric>> batchedMetricWorkloads = workload.chunk(this.config.executor.task_batch_size);
        final Proto.Event eventTemplate = schema.getEventTemplate();
        final boolean hasLuaHandlers = schema.getHandler() != null;
         batchedMetricWorkloads.collect((final RichIterable<Metric> metrics) -> new MetricProcessingTask(
                        metrics.asLazy(),
                        eventTemplate,
                        retrieverReference,
                        hasLuaHandlers
                )).collect(this.executor::submit);
        if (!this.config.ingest.async) {
            this.executor.resettingBarrier();
        }
        this.schemaProvider.refresh();
        if (this.config.ingest.schema_provider_locking) {
            this.schemaProvider.unlock();
        }
        this.executing = false;
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public MutableList<? super ForkJoinTask<?>> getTasksView() {
        return this.executor.getTasksView();
    }

}
