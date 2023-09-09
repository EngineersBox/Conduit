package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGenerator;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit_v2.processing.task.WaitableTaskExecutorPool;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit_v2.processing.task.worker.executor.JobExecutorPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.batch.WorkloadBatcher;
import com.engineersbox.conduit_v2.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import com.engineersbox.conduit_v2.schema.MetricsSchemaFactory;
import com.engineersbox.conduit_v2.schema.Schema;
import com.engineersbox.conduit_v2.schema.metric.Metric;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.jeasy.batch.core.job.JobReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final Parameters params;
    private final ConduitConfig config;
    private boolean executing = false;
    private ContentManager<?, ? ,?> contentManager;

    public Conduit(final Parameters params,
                   final ConduitConfig config) {
        this.params = params;
        this.config = config;
        this.params.validate();
    }

    public void execute(final IngestionContext context,
                        final Source<?> source) throws Exception {
        this.executing = true;
        final Schema schema = this.params.schemaProvider.provide(this.config.ingest.schema_provider_locking);
        if (this.params.schemaProvider.instanceRefreshed()) {
            LOGGER.debug("Schema provider triggered refresh, creating new content manager instance");
            this.contentManager = this.params.contentManagerFactory.construct(
                    schema,
                    source,
                    context,
                    this.params.ingesterFactory
            );
        }
        final LazyIterable<Metric> workload = schema.lazyMetricsView();
        this.contentManager.poll();
        final LazyIterable<RichIterable<Metric>> batchedMetricWorkloads = this.params.batcher.chunk(workload, this.config.executor.task_batch_size);
        LOGGER.debug("Partitioned workloads into {} batches of size at least {}", batchedMetricWorkloads.size(), this.config.executor.task_batch_size);
        final Proto.Event eventTemplate = schema.getEventTemplate();
        final ImmutableMap<String, Object> extensions = schema.getExtensions();
        batchedMetricWorkloads.collect((final RichIterable<Metric> metrics) -> this.params.workerTaskGenerator.generate(
                        metrics.asLazy(),
                        eventTemplate,
                        this.contentManager,
                        extensions,
                        this.params.contextInjector // TODO: Add support for supplying extension contexts and make this one that is provided
                )).forEach(this.params.executor::submit);
        LOGGER.debug("Submitted workloads to conduit executor");
        if (!this.config.ingest.async) {
            this.params.executor.resettingBarrier();
        }
        this.params.schemaProvider.refresh();
        if (this.config.ingest.schema_provider_locking) {
            this.params.schemaProvider.unlock();
        }
        this.executing = false;
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public RichIterable<? super ForkJoinTask<List<Future<JobReport>>>> getTasksView() {
        return this.params.executor.getTasksView();
    }

    public RichIterable<? super ForkJoinTask<List<Future<JobReport>>>> getTasksView(final long origin) {
        return this.params.executor.getTasksView(origin);
    }

    public static class Parameters {

        private MetricsSchemaFactory schemaProvider;
        private WaitableTaskExecutorPool executor;
        private TaskBatchGenerator workerTaskGenerator;
        private WorkloadBatcher batcher;
        private Consumer<ContextTransformer.Builder> contextInjector;
        private IngesterFactory ingesterFactory;
        private ContentManagerFactory contentManagerFactory;

        public Parameters() {
            this.workerTaskGenerator = TaskBatchGeneratorFactory.defaultGenerator();
            this.batcher = WorkloadBatcher.defaultbatcher();
            this.contextInjector = (_b) -> {};
            this.ingesterFactory = IngesterFactory.defaultFactory();
            this.contentManagerFactory = ContentManagerFactory.defaultFactory();
        }

        public Parameters setSchemaProvider(final MetricsSchemaFactory schemaProvider) {
            this.schemaProvider = schemaProvider;
            return this;
        }

        public Parameters setExecutor(final ClientPool clientProvider,
                                      final JobExecutorPool jobExecutorPool) {
            return setExecutor(
                    clientProvider,
                    jobExecutorPool,
                    Runtime.getRuntime().availableProcessors()
            );
        }

        public Parameters setExecutor(final ClientPool clientProvider,
                                      final JobExecutorPool jobExecutorPool,
                                      final int threadCount) {
            return setExecutor(new WaitableTaskExecutorPool(
                    clientProvider,
                    jobExecutorPool,
                    threadCount
            ));
        }

        public Parameters setExecutor(final WaitableTaskExecutorPool executor) {
            this.executor = executor;
            return this;
        }

        public Parameters setWorkerTaskGenerator(final TaskBatchGenerator workerTaskGenerator) {
            this.workerTaskGenerator = workerTaskGenerator;
            return this;
        }

        public Parameters setBatcher(final WorkloadBatcher batcher) {
            this.batcher = batcher;
            return this;
        }

        public Parameters setContextInjector(final Consumer<ContextTransformer.Builder> contextInjector) {
            this.contextInjector = contextInjector;
            return this;
        }

        public Parameters setIngesterFactory(final IngesterFactory ingesterFactory) {
            this.ingesterFactory = ingesterFactory;
            return this;
        }

        public Parameters setContentManagerFactory(ContentManagerFactory contentManagerFactory) {
            this.contentManagerFactory = contentManagerFactory;
            return this;
        }

        void validate() {
            final String message = Arrays.stream(getClass().getDeclaredFields())
                    .map((final Field field) -> {
                        field.setAccessible(true);
                        try {
                            if (field.get(this) == null) {
                                return " - " + field.getType().getSimpleName();
                            }
                        } catch (final IllegalAccessException e) {
                            // NOTE: Won't happen, within the same class
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
            if (!message.isEmpty()) {
                throw new IllegalStateException(String.format(
                        "Missing parameters of types for Conduit:%n%s",
                        message
                ));
            }
        }
    }

}
