package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.event.EventTransformer;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.AdapterProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.HandlerSaturationPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PostProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PreProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MetricProcessingTask implements ClientBoundWorkerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricProcessingTask.class);
    private static final String RIEMANN_CLIENT_CTX_ATTRIBUTE = "riemannClient";

    private final RichIterable<Metric> initialMetrics; // Received from conduit
    private final Proto.Event eventTemplate;
    private final EventTransformer transformer;
    private final AtomicReference<RetrievalHandler<Metric>> retriever;
    private final Pipeline.Builder<Metric> pipeline;
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

    private Pipeline.Builder<Metric> createPipeline(final boolean hasLuaHandlers) {
        final Pipeline.Builder<Metric> pipelineBuilder = new Pipeline.Builder<>();
        if (hasLuaHandlers) {
            pipelineBuilder.withStages(
                    new HandlerSaturationPipelineStage(),
                    new PreProcessFilterPipelineStage(
                            this.luaContextHandler,
                            this.contextTransformer
                    )
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
                    new AdapterProcessPipelineStage(
                            this.contextBuilder,
                            this.luaContextHandler,
                            this.contextTransformer,
                            this.eventTemplate
                    ),
                    new PostProcessFilterPipelineStage(
                            this.luaContextHandler,
                            this.contextTransformer
                    )
            );
        }
        return pipelineBuilder.withStage(new TerminatingPipelineStage<Proto.Event[]>("Send Riemann events") {
            @Override
            public void accept(final Proto.Event[] events) {
                final RiemannClient riemannClient = (RiemannClient) this.getContextAttribute(RIEMANN_CLIENT_CTX_ATTRIBUTE);
                try {
                    LOGGER.info("Sending events: \n" + Lists.fixedSize.of(events).stream().map((final Proto.Event event) -> String.format(
                            " - [Host: %s] [Service: %s] [State: '%s'] [Float: %f] [Double: %f] [Int: %d]%n",
                            event.getHost(),
                            event.getService(),
                            event.getState(),
                            event.getMetricF(),
                            event.getMetricD(),
                            event.getMetricSint64()
                    )).collect(Collectors.joining()));
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
        final Pipeline<Metric> metricPipeline = this.pipeline.withContext(RIEMANN_CLIENT_CTX_ATTRIBUTE, riemannClient)
                .build();
        this.initialMetrics.forEach(metricPipeline::accept);
    }

}
