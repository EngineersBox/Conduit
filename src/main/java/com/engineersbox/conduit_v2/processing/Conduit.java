package com.engineersbox.conduit_v2.processing;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.handler.LuaStdoutSink;
import com.engineersbox.conduit.handler.globals.LazyLoadedGlobalsProvider;
import com.engineersbox.conduit.schema.MetricsSchema;
import com.engineersbox.conduit.schema.MetricsSchemaProvider;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.config.ConduitConfig;
import com.engineersbox.conduit_v2.config.ConfigFactory;
import com.engineersbox.conduit_v2.processing.generation.TaskBatchGenerator;
import com.engineersbox.conduit_v2.processing.task.WaitableTaskExecutorPool;
import com.engineersbox.conduit_v2.processing.task.worker.client.ClientPool;
import com.engineersbox.conduit_v2.retrieval.content.ContentManager;
import com.engineersbox.conduit_v2.retrieval.content.ContentManagerFactory;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.Source;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.RichIterable;
import org.luaj.vm2.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class Conduit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conduit.class);

    private final MetricsSchemaProvider schemaProvider;
    private final WaitableTaskExecutorPool executor;
    private final ConduitConfig config;
    private boolean executing = false;
    private ContentManager<?, ?, ? ,?> contentManager;
    private final Consumer<ContextTransformer.Builder> contextInjector;
    private final TaskBatchGenerator workerTaskGenerator;

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final ClientPool clientProvider,
                   final TaskBatchGenerator workerTaskGenerator,
                   final Consumer<ContextTransformer.Builder> contextInjector,
                   final String configPath) {
        this(
                schemaProvider,
                clientProvider,
                workerTaskGenerator,
                contextInjector,
                ConfigFactory.create(configPath)
        );
    }

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final ClientPool clientProvider,
                   final TaskBatchGenerator workerTaskGenerator,
                   final Consumer<ContextTransformer.Builder> contextInjector,
                   final ConduitConfig config) {
        this(
                schemaProvider,
                new WaitableTaskExecutorPool(
                        clientProvider,
                        config.executor.task_pool_size.orElse(Runtime.getRuntime().availableProcessors())
                ),
                workerTaskGenerator,
                contextInjector,
                config
        );
    }

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final WaitableTaskExecutorPool executorPool,
                   final TaskBatchGenerator workerTaskGenerator,
                   final Consumer<ContextTransformer.Builder> contextInjector,
                   final String configPath) {
        this(
                schemaProvider,
                executorPool,
                workerTaskGenerator,
                contextInjector,
                ConfigFactory.create(configPath)
        );
    }

    public Conduit(final MetricsSchemaProvider schemaProvider,
                   final WaitableTaskExecutorPool executorPool,
                   final TaskBatchGenerator workerTaskGenerator,
                   final Consumer<ContextTransformer.Builder> contextInjector,
                   final ConduitConfig config) {
        this.schemaProvider = schemaProvider;
        this.executor = executorPool;
        this.workerTaskGenerator = workerTaskGenerator;
        this.config = config;
        this.contextInjector = contextInjector;
    }

    public void execute(final IngestionContext context,
                        final Source<?> source) throws Exception {
        this.executing = true;
        final MetricsSchema schema = this.schemaProvider.provide();
        if (this.config.ingest.schema_provider_locking) {
            this.schemaProvider.lock();
        }
        if (this.schemaProvider.instanceRefreshed()) {
            LOGGER.debug("Schema provider triggered refresh, creating new content manager instance");
            this.contentManager = ContentManagerFactory.construct(
                    schema,
                    source,
                    context,
                    Function.identity() // TODO: allow customisation via config
            );
        }
        final AtomicReference<RetrievalHandler<Metric>> retrieverReference = new AtomicReference<>(this.contentManager);
        final LazyIterable<Metric> workload = schema.valuesView().asLazy();
        this.contentManager.poll();
        final LazyIterable<RichIterable<Metric>> batchedMetricWorkloads = workload.chunk(this.config.executor.task_batch_size);
        LOGGER.debug("Partitioned workloads into {} batches of size at least {}", batchedMetricWorkloads.size(), this.config.executor.task_batch_size);
        final Proto.Event eventTemplate = schema.getEventTemplate();
        final LuaContextHandler handler = getHandler(schema.getHandler());
        batchedMetricWorkloads.collect((final RichIterable<Metric> metrics) -> this.workerTaskGenerator.generate(
                        metrics.asLazy(),
                        eventTemplate,
                        retrieverReference,
                        handler,
                        this.contextInjector
                )).forEach(this.executor::submit);
        LOGGER.debug("Submitted workloads to conduit executor");
        if (!this.config.ingest.async) {
            this.executor.resettingBarrier();
        }
        this.schemaProvider.refresh();
        if (this.config.ingest.schema_provider_locking) {
            this.schemaProvider.unlock();
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
        return this.executor.getTasksView();
    }

    public RichIterable<? super ForkJoinTask<?>> getTasksView(final long origin) {
        return this.executor.getTasksView(origin);
    }

}
