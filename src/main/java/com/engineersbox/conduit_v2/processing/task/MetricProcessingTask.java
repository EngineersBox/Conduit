package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit_v2.processing.event.EventTransformer;
import com.engineersbox.conduit_v2.processing.pipeline.*;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.AdapterProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.EventBatch;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PostProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PreProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import com.engineersbox.conduit_v2.schema.extension.LuaHandlerExtension;
import com.engineersbox.conduit_v2.schema.metric.Metric;
import io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.IRiemannClient;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.reader.IterableRecordReader;
import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.record.GenericRecord;
import org.jeasy.batch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MetricProcessingTask implements ClientBoundWorkerTask<List<JobReport>, JobExecutor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricProcessingTask.class);
    private static final String RIEMANN_CLIENT_CTX_ATTRIBUTE = "riemannClient";

    private final RichIterable<Metric> initialMetrics; // Received from conduit
    private final Proto.Event eventTemplate;
    private final EventTransformer transformer;
    private final RetrievalHandler<Metric> retriever;
    private final ContextTransformer.Builder contextBuilder;
    private final Consumer<ContextTransformer.Builder> contextInjector;

    public MetricProcessingTask(final RichIterable<Metric> metrics,
                                final Proto.Event eventTemplate,
                                final RetrievalHandler<Metric> retriever,
                                final ImmutableMap<String, Object> schemaExtensions,
                                final Consumer<ContextTransformer.Builder> contextInjector) {
        this.initialMetrics = metrics;
        this.transformer = new EventTransformer(eventTemplate);
        this.eventTemplate = eventTemplate;
        this.retriever = retriever;
        this.contextBuilder = ContextTransformer.builder(new ContextTransformer());
        this.contextInjector = contextInjector;
        LuaHandlerExtension luaHandlerExtension = null;
        if (schemaExtensions.get("lua_handlers") instanceof LuaHandlerExtension extension) {
            luaHandlerExtension = extension;
        }
    }

    private Pipeline.Builder<RichIterable<Metric>> createPipeline(final LuaHandlerExtension handlerExtension) {
        final Pipeline.Builder<RichIterable<Metric>> pipelineBuilder = new Pipeline.Builder<>();
        if (handlerExtension != null) {
            pipelineBuilder.withStages(
//                    new HandlerSaturationPipelineStage(),
                    new PreProcessFilterPipelineStage(
                            handlerExtension::getPreProcessHandleDefinition,
                            this.contextBuilder
                    ),
                    new PipelineStage<Metric, EventBatch>("Parse metrics events") {
                        @Override
                        public StageResult<EventBatch> invoke(final Metric metric) {
                            final Proto.Event[] result = MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                                    MetricProcessingTask.this.retriever.lookup(metric),
                                    metric.getStructure(),
                                    metric,
                                    0,
                                    ""
                            ).toArray(Proto.Event[]::new);
                            return new StageResult<>(
                                    StageResult.Type.COMBINE,
                                    (int) super.getContextAttribute(PreProcessFilterPipelineStage.FILTERED_COUNT_ATTRIBUTE),
                                    new EventBatch(
                                            result,
                                            metric.getExtensions()
                                    ),
                                    false
                            );
                        }
                    },
                    new AdapterProcessPipelineStage(
                            handlerExtension::getAdapterHandleDefinition,
                            this.contextBuilder,
                            this.eventTemplate
                    ),
                    new PostProcessFilterPipelineStage(
                            handlerExtension::getPostProcessHandleDefinition,
                            this.contextBuilder
                    )
            );
        } else {
            pipelineBuilder.withStage(new PipelineStage<Metric, Proto.Event[]>("Parse metrics events") {
                @Override
                public StageResult<Proto.Event[]> invoke(final Metric metric) {
                    final Proto.Event[] result = MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                            MetricProcessingTask.this.retriever.lookup(metric),
                            metric.getStructure(),
                            metric,
                            0,
                            ""
                    ).toArray(Proto.Event[]::new);
                    return new StageResult<>(
                            StageResult.Type.COMBINE,
                            (int) super.getContextAttribute(PreProcessFilterPipelineStage.FILTERED_COUNT_ATTRIBUTE),
                            result,
                            false
                    );
                }
            });
        }
        return pipelineBuilder.withStages(
                new TerminatingPipelineStage<Proto.Event[]>("Send Riemann events") {
                    @Override
                    public void accept(final Proto.Event[] events) {
                        final IRiemannClient riemannClient = (IRiemannClient) super.getContextAttribute(RIEMANN_CLIENT_CTX_ATTRIBUTE);
                        try {
                            LOGGER.info(
                                    "Sending events: {}",
                                    Arrays.stream(events)
                                            .map((final Proto.Event event) -> String.format(
                                                    "%n - [Host: %s] [Description: %s] [Service: %s] [State: '%s'] [Float: %f] [Double: %f] [Int: %d] [Time: %d] [TTL: %f] [Tags: %s] [Attributes: %s]",
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

    private JobBuilder<Metric, Proto.Event[]> createTransformerJob(final PipelineProcessingModel model) {
        return model.<Metric, Proto.Event[]>addJob("Metric Transformer")
                .processor((final Record<Metric> record) -> {
                    final Metric metric = record.getPayload();
                    final Proto.Event[] result = MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                            MetricProcessingTask.this.retriever.lookup(metric),
                            metric.getStructure(),
                            metric,
                            0,
                            ""
                    ).toArray(Proto.Event[]::new);
                    return new GenericRecord<>(
                            record.getHeader(),
                            result
                    );
                });
    }

    private JobBuilder<Proto.Event[], Proto.Event[]> createRiemannSendJob(final PipelineProcessingModel model,
                                      final IRiemannClient riemannClient) {
        return model.<Proto.Event[], Proto.Event[]>addJob("Send Riemann Events")
                .writer((final Batch<Proto.Event[]> batch) -> {
                    try {
                        for (final Record<Proto.Event[]> eventsRecord : batch) {
                            final Proto.Event[] events = eventsRecord.getPayload();
                            LOGGER.info(
                                    "Sending events: {}",
                                    Arrays.stream(events)
                                            .map((final Proto.Event event) -> String.format(
                                                    "%n - [Host: %s] [Description: %s] [Service: %s] [State: '%s'] [Float: %f] [Double: %f] [Int: %d] [Time: %d] [TTL: %f] [Tags: %s] [Attributes: %s]",
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
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public ProcessingModel<List<JobReport>, JobExecutor> apply(final IRiemannClient riemannClient) {
        final PipelineProcessingModel model = new PipelineProcessingModel();
        final JobBuilder<Metric, Proto.Event[]> transformerJob = createTransformerJob(model);
        final JobBuilder<Proto.Event[], Proto.Event[]> riemannSendJob = createRiemannSendJob(model, riemannClient);
        final SpscLinkedQueue<Record<Proto.Event[]>> queue = new SpscLinkedQueue<>();
        model.connectJobs(
                transformerJob,
                riemannSendJob,
                queue
        );
        transformerJob.reader(new IterableRecordReader<>(this.initialMetrics))
                .writer((final Batch<Proto.Event[]> batch) -> batch.forEach(queue::offer));
        riemannSendJob.reader(queue::poll);
        return model;
    }

}
