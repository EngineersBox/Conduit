package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import com.engineersbox.conduit_v2.processing.task.TaskBatcher;
import com.engineersbox.conduit_v2.processing.task.TaskExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.client.RiemannClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider;
    private final TaskExecutorPool executor;
    private final ConduitConfig config;
    private boolean executing = false;
    private final List<? super ForkJoinTask<?>> tasks;
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
        this.executor = new TaskExecutorPool(clientProvider);
        this.config = config;
        this.tasks = new ArrayList<>();
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
        final Stream<List<Metric>> batchedMetricWorkloads = Stream.of();
        // TODO: Update this when MetricSchema changed to use new Metric class or new metric class replaced with old one
        /* final Stream<List<Metric>> batchedMetricWorkloads = */ TaskBatcher.partitioned(
                schema.values(),
                this.config.executor.batch_size,
                this.config.executor.parallel_batching
        );
        this.contentManager.poll();
        submitTasks(
                batchedMetricWorkloads.map((final List<Metric> metrics) -> new MetricProcessingTask(
                        metrics,
                        schema.getEventTemplate(),
                        retrieverReference,
                        schema.getHandler() != null
                )).toArray(MetricProcessingTask[]::new)
        );

        this.schemaProvider.refresh();
        if (this.config.ingest.schema_provider_locking) {
            this.schemaProvider.unlock();
        }
        this.executing = false;
        this.tasks.clear();
    }

    private void submitTasks(final MetricProcessingTask ...tasks) {
        if (this.config.ingest.async) {
            // NOTE: For complete async behaviour need async = true and schemaProviderLocking = false
            this.executor.invokeAll(
                    this.tasks::add,
                    tasks
            );
        }
        this.executor.waitAll(tasks);
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public List<? super ForkJoinTask<?>> getTasks() {
        return this.config.ingest.async ? this.tasks : null;
    }

}
