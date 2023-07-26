package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.handler.LuaStdoutSink;
import com.engineersbox.conduit.handler.globals.LazyLoadedGlobalsProvider;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGeneratorFactory;
import com.engineersbox.conduit_v2.processing.schema.MetricsSchemaProvider;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGenerator;
import com.engineersbox.conduit_v2.processing.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.schema.Schema;
import com.engineersbox.conduit_v2.processing.task.WaitableTaskExecutorPool;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import com.engineersbox.conduit_v2.retrieval.content.batch.WorkloadBatcher;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import io.riemann.riemann.Proto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;
import org.luaj.vm2.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final Parameters params;
    private final ConduitConfig config;
    private boolean executing = false;
    private ContentManager<?, ?, ? ,?> contentManager;

    public Conduit(final Parameters params,
                   final String configPath) {
        this(
                params,
                ConfigFactory.create(configPath)
        );
    }

    public Conduit(final Parameters params,
                   final ConduitConfig config) {
        this.params = params;
        this.config = config;
        this.params.validate();
    }

    public void execute(final IngestionContext context,
                        final Source<?> source) throws Exception {
        this.executing = true;
        final Schema schema = this.params.schemaProvider.provide();
        if (this.config.ingest.schema_provider_locking) {
            this.params.schemaProvider.lock();
        }
        if (this.params.schemaProvider.instanceRefreshed()) {
            LOGGER.debug("Schema provider triggered refresh, creating new content manager instance");
            this.contentManager = ContentManagerFactory.construct(
                    schema,
                    source,
                    context,
                    Function.identity() // TODO: allow customisation via config
            );
        }
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(this.contentManager);
        final LazyIterable<Metric> workload = schema.lazyMetricsView();
        this.contentManager.poll();
        final LazyIterable<RichIterable<Metric>> batchedMetricWorkloads = this.params.batcher.chunk(workload, this.config.executor.task_batch_size);
        LOGGER.debug("Partitioned workloads into {} batches of size at least {}", batchedMetricWorkloads.size(), this.config.executor.task_batch_size);
        final Proto.Event eventTemplate = schema.getEventTemplate();
        // TODO: Move lua stuff to be a schema extension with supplementary processing bindings
        final LuaContextHandler handler = schema.requiresJseGlobals() ? getHandler(schema.getHandler()) : null;
        batchedMetricWorkloads.collect((final RichIterable<Metric> metrics) -> this.params.workerTaskGenerator.generate(
                        metrics.asLazy(),
                        eventTemplate,
                        retrieverReference,
                        handler,
                        this.params.contextInjector
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

    private LuaContextHandler getHandler(final Path handlerLocation) {
        if (handlerLocation == null) {
            return null;
        }
        final LazyLoadedGlobalsProvider globalsProvider =  new LazyLoadedGlobalsProvider(
                this::configureGlobals,
                false
        );
        return new LuaContextHandler(
                handlerLocation.toAbsolutePath().toString(),
                globalsProvider
        );
    }

    private Globals configureGlobals(final Globals standard) {
        standard.STDOUT = LuaStdoutSink.createSlf4j(
                this.config.handler.name,
                Level.valueOf(this.config.handler.level.name())
        );
        return standard;
    }

    public boolean isExecuting() {
        return this.executing;
    }

    public RichIterable<? super ForkJoinTask<?>> getTasksView() {
        return this.params.executor.getTasksView();
    }

    public RichIterable<? super ForkJoinTask<?>> getTasksView(final long origin) {
        return this.params.executor.getTasksView(origin);
    }

    public static class Parameters {

        private MetricsSchemaProvider schemaProvider;
        private WaitableTaskExecutorPool executor;
        private TaskBatchGenerator workerTaskGenerator;
        private WorkloadBatcher batcher;
        private Consumer<ContextTransformer.Builder> contextInjector;

        public Parameters() {
            this.workerTaskGenerator = TaskBatchGeneratorFactory.defaultGenerator();
            this.batcher = WorkloadBatcher.defaultbatcher();
            this.contextInjector = (_b) -> {};
        }

        public Parameters setSchemaProvider(final MetricsSchemaProvider schemaProvider) {
            this.schemaProvider = schemaProvider;
            return this;
        }

        public Parameters setExecutor(final ClientPool clientProvider) {
            return setExecutor(clientProvider, Runtime.getRuntime().availableProcessors());
        }

        public Parameters setExecutor(final ClientPool clientProvider, final int threadCount) {
            return setExecutor(new WaitableTaskExecutorPool(clientProvider, threadCount));
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
