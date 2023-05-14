package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.event.EventDeserialiser;
import com.engineersbox.conduit_v2.processing.event.EventSerialiser;
import com.engineersbox.conduit_v2.processing.event.EventTransformer;
import com.engineersbox.conduit_v2.processing.event.EventsDeserialiser;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.eclipse.collections.api.RichIterable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MetricProcessingTask implements ClientBoundWorkerTask {

    private static final String RIEMANN_CLIENT_CTX_ATTRIBUTE = "riemannClient";
    private static final String LUA_HANDLER_PREFIX = "luaHandler_";

    private final RichIterable<Metric> initialMetrics; // Received from conduit
    private final Proto.Event eventTemplate;
    private final EventTransformer transformer;
    private final AtomicReference<RetrievalHandler<Metric>> retriever;
    private final Pipeline.Builder<RichIterable<Metric>> pipeline;
    private final LuaContextHandler luaContextHandler;
    private final ContextTransformer contextTransformer;
    private final ContextTransformer.Builder contextBuilder;
    private final Consumer<ContextTransformer.Builder> contextInjector;

    public MetricProcessingTask(final RichIterable<Metric> metrics,
                                final Proto.Event eventTemplate,
                                final AtomicReference<RetrievalHandler<Metric>> retriever,
                                final LuaContextHandler luaContextHandler,
                                final Consumer<ContextTransformer.Builder> contextInjector) {
        this.initialMetrics = metrics;
        this.transformer = new EventTransformer(eventTemplate);
        this.eventTemplate = eventTemplate;
        this.retriever = retriever;
        this.pipeline = createPipeline(luaContextHandler != null);
        this.luaContextHandler = luaContextHandler;
        this.contextTransformer = new ContextTransformer();
        this.contextBuilder = ContextTransformer.builder(this.contextTransformer);
        this.contextInjector = contextInjector;
    }

    private Pipeline.Builder<RichIterable<Metric>> createPipeline(final boolean hasLuaHandlers) {
        final Pipeline.Builder<RichIterable<Metric>> pipelineBuilder = new Pipeline.Builder<>();
        if (hasLuaHandlers) {
            pipelineBuilder.withStages(
                    new PipelineStage<Metric, Metric>("Handlers Saturation") {
                        @Override
                        public Metric invoke(final Metric previousResult) {
                            previousResult.getHandlers().forEachKeyValue((final String name, final String handler) ->
                                    setContextAttribute(LUA_HANDLER_PREFIX + name, handler)
                            );
                            return previousResult;
                        }
                    },
                    new FilterPipelineStage<Metric>("Pre-process Lua filter") {
                        @Override
                        public boolean test(final Metric metric) {
                            final Object handlerObj = getContextAttribute(LUA_HANDLER_PREFIX + "pre_process");
                            if (!(handlerObj instanceof String handler)) {
                                return true;
                            }
                            MetricProcessingTask.this.luaContextHandler.invoke(
                                    handler,
                                    MetricProcessingTask.this.contextTransformer.transform()
                            );
                            return MetricProcessingTask.this.luaContextHandler.getFromResult(
                                    new String[]{
                                            "executionContext",
                                            "shouldRun"
                                    },
                                    boolean.class
                            );
                        }
                    }
            );
        }
        pipelineBuilder.withStage(new ProcessPipelineStage<Metric, Proto.Event[]>("Parse metrics events") {
            @Override
            public Proto.Event[] apply(final Metric metric) {
                return MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                        MetricProcessingTask.this.retriever.get().lookup(metric),
                        metric.getType(),
                        metric,
                        0,
                        ""
                ).toArray(Proto.Event[]::new);
            }
        });
        if (hasLuaHandlers) {
            pipelineBuilder.withStages(
                    new ProcessPipelineStage<Proto.Event[], Proto.Event[]>("Adapter Lua handler") {
                        @Override
                        public Proto.Event[] apply(final Proto.Event[] events) {
                            final Object handlerObj = getContextAttribute(LUA_HANDLER_PREFIX + "adapter");
                            if (!(handlerObj instanceof String handler)) {
                                return events;
                            }
                            MetricProcessingTask.this.contextBuilder.withReadOnly(
                                    "events",
                                    events,
                                    EventSerialiser.class
                            );
                            MetricProcessingTask.this.luaContextHandler.invoke(
                                    handler,
                                    MetricProcessingTask.this.contextTransformer.transform()
                            );
                            return MetricProcessingTask.this.luaContextHandler.getFromResult(
                                    new String[]{
                                            "events"
                                    },
                                    new EventsDeserialiser(MetricProcessingTask.this.eventTemplate)
                            );
                        }
                    },
                    new FilterPipelineStage<Proto.Event[]>("Post-process Lua filter") {
                        @Override
                        public boolean test(final Proto.Event[] element) {
                            final Object handlerObj = getContextAttribute(LUA_HANDLER_PREFIX + "post_process");
                            if (!(handlerObj instanceof String handler)) {
                                return true;
                            }
                            MetricProcessingTask.this.luaContextHandler.invoke(
                                    handler,
                                    MetricProcessingTask.this.contextTransformer.transform()
                            );
                            return MetricProcessingTask.this.luaContextHandler.getFromResult(
                                    new String[]{
                                            "executionContext",
                                            "shouldRun"
                                    },
                                    boolean.class
                            );
                        }
                    }
            );
        }
        return pipelineBuilder.withStage(new TerminatingPipelineStage<Proto.Event[]>("Send Riemann events") {
            @Override
            public void accept(final Proto.Event[] events) {
                final RiemannClient riemannClient = (RiemannClient) this.getContextAttribute(RIEMANN_CLIENT_CTX_ATTRIBUTE);
                try {
                    riemannClient.sendEvents(events).deref(1, TimeUnit.SECONDS);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void accept(final RiemannClient riemannClient) {
        this.contextInjector.accept(this.contextBuilder);
        this.pipeline.withContext(RIEMANN_CLIENT_CTX_ATTRIBUTE, riemannClient)
                .build()
                .accept(this.initialMetrics);
    }

}
