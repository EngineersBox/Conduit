package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.event.EventTransformer;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.AdapterProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.HandlerSaturationPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PostProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PreProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.ByteString;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.IRiemannClient;
import io.riemann.riemann.client.RiemannClient;
import org.eclipse.collections.api.RichIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
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
        this.luaContextHandler = luaContextHandler;
        this.contextTransformer = new ContextTransformer();
        this.contextBuilder = ContextTransformer.builder(this.contextTransformer);
        this.contextInjector = contextInjector;
        this.pipeline = createPipeline(luaContextHandler != null);
    }

    private Pipeline.Builder<RichIterable<Metric>> createPipeline(final boolean hasLuaHandlers) {
        final Pipeline.Builder<RichIterable<Metric>> pipelineBuilder = new Pipeline.Builder<>();
        if (hasLuaHandlers) {
            pipelineBuilder.withStages(
                    new HandlerSaturationPipelineStage()
            );
        }
        pipelineBuilder.withStages(
                new PreProcessFilterPipelineStage(
                        this.luaContextHandler,
                        this.contextTransformer,
                        hasLuaHandlers
                ),
                new PipelineStage<Metric, Proto.Event[]>("Parse metrics events") {
                    @Override
                    public StageResult<Proto.Event[]> invoke(final Metric metric) {
                        final Proto.Event[] result = MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                                MetricProcessingTask.this.retriever.get().lookup(metric),
                                metric.getType(),
                                metric,
                                0,
                                ""
                        ).toArray(Proto.Event[]::new);
                        return new StageResult<>(
                                StageResult.Type.COMBINE,
                                (int) getContextAttribute(PreProcessFilterPipelineStage.FILTERED_COUNT_ATTRIBUTE),
                                result,
                                false
                        );
                    }
                }
        );
        if (hasLuaHandlers) {
            pipelineBuilder.withStages(
                    new AdapterProcessPipelineStage(
                            this.contextBuilder,
                            this.luaContextHandler,
                            this.contextTransformer,
                            this.eventTemplate
                    )
            );
        }
        return pipelineBuilder.withStages(
                new PostProcessFilterPipelineStage(
                        this.luaContextHandler,
                        this.contextTransformer,
                        hasLuaHandlers
                ),
                new TerminatingPipelineStage<Proto.Event[]>("Send Riemann events") {
                    @Override
                    public void accept(final Proto.Event[] events) {
                        final RiemannClient riemannClient = (RiemannClient) this.getContextAttribute(RIEMANN_CLIENT_CTX_ATTRIBUTE);
                        try {
                            LOGGER.info(
                                    "Sending events: {}",
                                    Arrays.stream(events)
                                            .map((final Proto.Event event) -> String.format(
                                                    "%n\t- [Host: %s] [Description: %s] [Service: %s] [State: '%s'] [Float: %f] [Double: %f] [Int: %d] [Time: %d] [TTL: %f] [Tags: %s] [Attributes: %s]",
                                                    event.getHost(),
                                                    event.getDescription(),
                                                    event.getService(),
                                                    event.getState(),
                                                    event.getMetricF(),
                                                    event.getMetricD(),
                                                    event.getMetricSint64(),
                                                    event.getTimeMicros(),
                                                    event.getTtl(),
                                                    String.join(", ", event.getTagsList()),
                                                    event.getAttributesList()
                                                            .stream()
                                                            .map((final Proto.Attribute attr) -> String.format(
                                                                    "{ key: \"%s\", value: \"%s\" }",
                                                                    attr.getKey(),
                                                                    attr.getValue()
                                                            ))
                                                            .collect(Collectors.joining(", "))
                                            )).collect(Collectors.joining())
                            );
                            riemannClient.sendEvents(events).deref(1, TimeUnit.SECONDS);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @Override
    public void accept(final IRiemannClient riemannClient) {
        this.contextInjector.accept(this.contextBuilder);
        try {
            this.pipeline.withContext(RIEMANN_CLIENT_CTX_ATTRIBUTE, riemannClient)
                    .build()
                    .accept(this.initialMetrics);
        } catch (final Exception e) {
            LOGGER.error("Exception during metric processing pipeline invocation:", e);
        }
    }

}
