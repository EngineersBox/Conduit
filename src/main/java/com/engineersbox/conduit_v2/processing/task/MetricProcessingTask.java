package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit_v2.processing.EventTransformer;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.RiemannClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MetricProcessingTask implements ClientBoundWorkerTask {

    private static final String RIEMANN_CLIENT_CTX_ATTRIBUTE = "riemannClient";

    private final List<Metric> initialMetrics; // Received from pipeline
    private final EventTransformer transformer;
    private final AtomicReference<RetrievalHandler<Metric>> retriever;
    private final Pipeline.Builder<List<Metric>> pipeline;

    public MetricProcessingTask(final List<Metric> metrics,
                                final Proto.Event eventTemplate,
                                final AtomicReference<RetrievalHandler<Metric>> retriever,
                                final boolean hasLuaHandlers) {
        this.initialMetrics = metrics;
        this.transformer = new EventTransformer(eventTemplate);
        this.retriever = retriever;
        this.pipeline = createPipeline(hasLuaHandlers);
    }

    private Pipeline.Builder<List<Metric>> createPipeline(final boolean hasLuaHandlers) {
        final Pipeline.Builder<List<Metric>> pipelineBuilder = new Pipeline.Builder<List<Metric>>();
        if (hasLuaHandlers) {
            pipelineBuilder.withStage(new FilterPipelineStage<Metric>("Pre-process Lua filter") {
                @Override
                public boolean test(final Metric metric) {
                    // TODO: Invoke pre-process Lua handler and return inclusion state
                    return true;
                }
            });
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
            }).withStage(new ProcessPipelineStage<Proto.Event[], Proto.Event[]>("Post-process Lua handlers") {
                @Override
                public Proto.Event[] apply(final Proto.Event[] events) {
                    // TODO: Invoke post-process Lua handlers for modifying events
                    return events;
                }
            });
        if (hasLuaHandlers) {
            pipelineBuilder.withStage(new FilterPipelineStage<Proto.Event[]>("Post-process Lua filter") {
                @Override
                public boolean test(final Proto.Event[] element) {
                    // TODO: Invoke post-process Lua handler and return inclusion state
                    return false;
                }
            });
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
        this.pipeline.withContext(RIEMANN_CLIENT_CTX_ATTRIBUTE, riemannClient)
                .build()
                .accept(this.initialMetrics);
    }

}
