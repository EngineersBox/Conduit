package com.engineersbox.conduit.core.processing.task;

import com.engineersbox.conduit.core.processing.event.EventTransformer;
import com.engineersbox.conduit.core.processing.pipeline.PipelineProcessingModel;
import com.engineersbox.conduit.core.processing.pipeline.ProcessingModel;
import com.engineersbox.conduit.core.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit.core.retrieval.content.RetrievalHandler;
import com.engineersbox.conduit.core.schema.extension.LuaHandlerExtension;
import com.engineersbox.conduit.core.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.util.Functional;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.IRiemannClient;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.jctools.queues.SpscLinkedQueue;
import org.jctools.queues.atomic.SpscLinkedAtomicQueue;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.listener.BatchListener;
import org.jeasy.batch.core.reader.IterableRecordReader;
import org.jeasy.batch.core.reader.RecordReader;
import org.jeasy.batch.core.record.Batch;
import org.jeasy.batch.core.record.GenericRecord;
import org.jeasy.batch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MetricProcessingTask implements ClientBoundWorkerTask<List<Future<JobReport>>, JobExecutor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricProcessingTask.class);
    private static final int BATCH_SIZE = 5;

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
        if (schemaExtensions != null
            && schemaExtensions.get(LuaHandlerExtension.SCHEMA_EXTENSION_FIELD_NAME) instanceof LuaHandlerExtension extension) {
            luaHandlerExtension = extension;
        }
    }

    // Commented out code is the old pipeline implementation with lua. Need to refactor this into
    // a form supported by the new extensions system and include it here with the new stuff.
    /*
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
                                    metric
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
                            metric
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
                        final IRiemannClient riemannClient = (IRiemannClient) super.getContextAttribute("riemannClient");
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
    } */

    private JobBuilder<Metric, Proto.Event[]> createTransformerJob(final PipelineProcessingModel model,
                                                                   final AtomicBoolean endIndicator) {
        return model.<Metric, Proto.Event[]>addJob("Parse Metric Events")
                .batchSize(BATCH_SIZE)
                .processor((final Record<Metric> record) -> {
                    final Metric metric = record.getPayload();
                    final Proto.Event[] result = MetricProcessingTask.this.transformer.parseCoerceMetricEvents(
                            MetricProcessingTask.this.retriever.lookup(metric),
                            metric.getStructure(),
                            metric
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
                .batchSize(BATCH_SIZE)
                .writer((final Batch<Proto.Event[]> batch) -> {
                    try {
                        LOGGER.debug("Batch size {}, Batch: {}", batch.size(), batch);
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
                            riemannClient.sendEvents(events).deref(
                                    1,
                                    TimeUnit.SECONDS
                            );
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public ProcessingModel<List<Future<JobReport>>, JobExecutor> apply(final IRiemannClient riemannClient) {
        final PipelineProcessingModel model = new PipelineProcessingModel(false);
        final AtomicBoolean endIndicator = new AtomicBoolean(false);
        final JobBuilder<Metric, Proto.Event[]> transformerJob = createTransformerJob(model, endIndicator);
        final JobBuilder<Proto.Event[], Proto.Event[]> riemannSendJob = createRiemannSendJob(model, riemannClient);
//        final SpscLinkedQueue<Record<Proto.Event[]>> queue = new SpscLinkedQueue<>();
//        final LinkedBlockingQueue<Record<Proto.Event[]>> queue = new LinkedBlockingQueue<>();
        final SpscLinkedAtomicQueue<Record<Proto.Event[]>> queue = new SpscLinkedAtomicQueue<>();
//        final ArrayBlockingQueue<Record<Proto.Event[]>> queue = new ArrayBlockingQueue<>(10);
        model.connectJobs(
                transformerJob,
                (final Batch<Proto.Event[]> batch) -> batch.forEach(queue::offer),
                riemannSendJob,
                () -> {
                    Record<Proto.Event[]> record;
                    // TODO: Convert to BlockingQueue with take/put sleep/signal semantics to avoid busy loop
                    //       will need to tie dependent job close to close this job as well to avoid end indicator
                    while ((record = queue.poll()) == null && !endIndicator.get());
                    return record;
                },
                queue
        );
        transformerJob.reader(new IterableRecordReader<>(this.initialMetrics){
            @Override
            public void close() throws Exception {
                endIndicator.set(true);
                super.close();
            }
        });
        // TODO: Finish implementing jobs
        return model;
    }

}
