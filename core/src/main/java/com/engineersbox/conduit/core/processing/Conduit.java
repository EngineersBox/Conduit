package com.engineersbox.conduit.core.processing;

import com.engineersbox.conduit.core.config.ConfigFactory;
import com.engineersbox.conduit.core.processing.task.WaitableTaskExecutorPool;
import com.engineersbox.conduit.core.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit.core.processing.task.worker.executor.JobExecutorPool;
import com.engineersbox.conduit.core.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.core.config.ConduitConfig;
import com.engineersbox.conduit.core.processing.generation.TaskBatchGenerator;
import com.engineersbox.conduit.core.retrieval.content.ContentManager;
import com.engineersbox.conduit.core.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit.core.retrieval.content.batch.WorkloadBatcher;
import com.engineersbox.conduit.core.retrieval.ingest.IngesterFactory;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.source.Source;
import com.engineersbox.conduit.core.schema.factory.MetricsSchemaFactory;
import com.engineersbox.conduit.core.schema.Schema;
import com.engineersbox.conduit.core.schema.metric.Metric;
import io.riemann.riemann.Proto;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ThreadSafe
public class Conduit<T, E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final Parameters<T, E> params;
    private final ConduitConfig config;
    private boolean executing = false;
    private ContentManager<?, ? ,?> contentManager;

    public Conduit(@Nonnull final Parameters<T, E> params) {
        this(
                params,
                ConfigFactory.createDefault()
        );
    }

    public Conduit(@Nonnull final Parameters<T, E> params,
                   @Nonnull final ConduitConfig config) {
        this.params = params;
        this.config = config;
        this.params.validate();
    }

    private RichIterable<Metric> retrieveWorkload(final Schema schema,
                                                  final IngestionContext context,
                                                  final Source<?> source) {
        if (this.params.schemaProvider.instanceRefreshed()) {
            LOGGER.debug("Schema provider triggered refresh, creating new content manager instance");
            this.contentManager = this.params.contentManagerFactory.construct(
                    schema,
                    source,
                    context,
                    this.params.ingesterFactory
            );
            this.contentManager.setCacheKey(this.params.cacheKey);
        }
        RichIterable<Metric> workload = schema.metricsView();
        if (this.config.executor.lazy) {
            workload = workload.asLazy();
        }
        return workload;
    }

    private RichIterable<ForkJoinTask<T>> submitWorkload(final Schema schema,
                                                         final RichIterable<Metric> workload) {
        final RichIterable<RichIterable<Metric>> batchedMetricWorkloads = this.params.batcher.chunk(workload, this.config.executor.task_batch_size);
        LOGGER.debug("Partitioned workloads into {} batches of size at least {}", batchedMetricWorkloads.size(), this.config.executor.task_batch_size);
        final Proto.Event eventTemplate = schema.getEventTemplate();
        final ImmutableMap<String, Object> extensions = schema.getExtensions();
        final RichIterable<ForkJoinTask<T>> results = batchedMetricWorkloads.collect((final RichIterable<Metric> metrics) -> this.params.workerTaskGenerator.generate(
                metrics.asLazy(),
                eventTemplate,
                this.contentManager,
                extensions,
                this.params.contextInjector // TODO: Add support for supplying extension contexts and make this one that is provided
        )).collect(this.params.executor::submit);
        LOGGER.debug("Submitted workloads to conduit executor");
        return results;
    }

    private void synchronisedRefresh() {
        if (!this.config.ingest.async) {
            this.params.executor.resettingBarrier(this.config.executor.lazy);
        }
        this.params.schemaProvider.refresh();
        if (this.config.ingest.schema_provider_locking) {
            this.params.schemaProvider.unlock();
        }
    }

    public RichIterable<ForkJoinTask<T>> execute() throws Exception {
        return execute(null);
    }

    public RichIterable<ForkJoinTask<T>> execute(@Nullable IngestionContext context) throws Exception {
        return execute(
                context,
                Source.singleConfigurable()
        );
    }

    public RichIterable<ForkJoinTask<T>> execute(@Nullable IngestionContext context,
                                                 @Nonnull final Source<?> source) throws Exception {
        this.executing = true;
        if (context == null) {
            context = IngestionContext.defaultContext();
        }
        final Schema schema = this.params.schemaProvider.provide(this.config.ingest.schema_provider_locking);
        final RichIterable<Metric> workload = retrieveWorkload(schema, context, source);
        this.contentManager.poll();
        final RichIterable<ForkJoinTask<T>> results = submitWorkload(
                schema,
                workload
        );
        synchronisedRefresh();
        this.executing = false;
        return results;
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public RichIterable<ForkJoinTask<T>> getTasksView() {
        return this.params.executor.getTasksView();
    }

    public RichIterable<ForkJoinTask<T>> getTasksView(final long origin) {
        return this.params.executor.getTasksView(origin);
    }

    public static class Parameters<T, E> {

        private static final Logger LOGGER = LoggerFactory.getLogger(Parameters.class);

        private MetricsSchemaFactory schemaProvider;
        private WaitableTaskExecutorPool<T, E> executor;
        private TaskBatchGenerator<T, E> workerTaskGenerator;
        private WorkloadBatcher batcher;
        private Consumer<ContextTransformer.Builder> contextInjector;
        private IngesterFactory ingesterFactory;
        private ContentManagerFactory contentManagerFactory;
        private Optional<String> cacheKey;

        public Parameters() {
            this.batcher = WorkloadBatcher.defaultBatcher();
            this.contextInjector = (_b) -> {};
            this.ingesterFactory = IngesterFactory.defaultFactory();
            this.contentManagerFactory = ContentManagerFactory.defaultFactory();
            this.cacheKey = Optional.empty();
        }

        public Parameters<T, E> setSchemaProvider(final MetricsSchemaFactory schemaProvider) {
            this.schemaProvider = schemaProvider;
            return this;
        }

        public Parameters<T, E> setExecutor(final ClientPool clientProvider,
                                            final JobExecutorPool<E> jobExecutorPool) {
            return setExecutor(
                    clientProvider,
                    jobExecutorPool,
                    Runtime.getRuntime().availableProcessors()
            );
        }

        public Parameters<T, E> setExecutor(final ClientPool clientProvider,
                                            final JobExecutorPool<E> jobExecutorPool,
                                            final int threadCount) {
            return setExecutor(new WaitableTaskExecutorPool<>(
                    clientProvider,
                    jobExecutorPool,
                    threadCount
            ));
        }

        public Parameters<T, E> setExecutor(final WaitableTaskExecutorPool<T, E> executor) {
            this.executor = executor;
            return this;
        }

        public Parameters<T, E> setWorkerTaskGenerator(final TaskBatchGenerator<T, E> workerTaskGenerator) {
            this.workerTaskGenerator = workerTaskGenerator;
            return this;
        }

        public Parameters<T, E> setBatcher(final WorkloadBatcher batcher) {
            this.batcher = batcher;
            return this;
        }

        public Parameters<T, E> setContextInjector(final Consumer<ContextTransformer.Builder> contextInjector) {
            this.contextInjector = contextInjector;
            return this;
        }

        public Parameters<T, E> setIngesterFactory(final IngesterFactory ingesterFactory) {
            this.ingesterFactory = ingesterFactory;
            return this;
        }

        public Parameters<T, E> setContentManagerFactory(ContentManagerFactory contentManagerFactory) {
            this.contentManagerFactory = contentManagerFactory;
            return this;
        }

        public Parameters<T, E> setCacheKey(final String cacheKey) {
            this.cacheKey = Optional.ofNullable(cacheKey);
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
                            // NOTE: Won't happen, since the class fields are
                            //       not final... well unless I do a stupid
                            //       that is. So not entirely impossible.
                            throw new IllegalStateException(String.format(
                                    "Unable to access field %s in parameters for validation",
                                    field.getName()
                            ), e);
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));
            if (StringUtils.isNotBlank(message)) {
                throw new IllegalStateException(String.format(
                        "Missing parameters of types for Conduit:%n%s",
                        message
                ));
            }
        }
    }

}
