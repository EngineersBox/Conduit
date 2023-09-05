package com.engineersbox.conduit_v2.processing.task;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit_v2.processing.event.EventTransformer;
import com.engineersbox.conduit_v2.processing.pipeline.Pipeline;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.TerminatingPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.AdapterProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.EventBatch;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PostProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.lua.PreProcessFilterPipelineStage;
import com.engineersbox.conduit_v2.schema.extension.LuaHandlerExtension;
import com.engineersbox.conduit_v2.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundWorkerTask;
import com.engineersbox.conduit_v2.retrieval.content.RetrievalHandler;
import io.riemann.riemann.Proto;
import io.riemann.riemann.client.IRiemannClient;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.jeasy.batch.core.job.Job;
import org.jeasy.batch.core.job.JobBuilder;
import org.jeasy.batch.core.job.JobExecutor;
import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.processor.RecordProcessor;
import org.jeasy.batch.core.reader.IterableRecordReader;
import org.jeasy.batch.core.record.GenericRecord;
import org.jeasy.batch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MetricProcessingTask implements ClientBoundWorkerTask {

    /* TODO: Refactor usage of Pipeline to use EasyBatch library
     *       to use a more effective and standardised interface
     *       including fork-join-esque queued parallel pipeline
     *       structures as exemplified by the parallel tutorial
     *       https://github.com/j-easy/easy-batch/tree/master/easy-batch-tutorials/src/main/java/org/jeasy/batch/tutorials/advanced/parallel
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricProcessingTask.class);
    private static final String RIEMANN_CLIENT_CTX_ATTRIBUTE = "riemannClient";

    private final RichIterable<Metric> initialMetrics; // Received from conduit
    private final Proto.Event eventTemplate;
    private final EventTransformer transformer;
    private final RetrievalHandler<Metric> retriever;
    private final Pipeline.Builder<RichIterable<Metric>> pipeline;
    private final ContextTransformer.Builder contextBuilder;
    private final Consumer<ContextTransformer.Builder> contextInjector;
    private final JobExecutor executor;

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
        this.pipeline = createPipeline(luaHandlerExtension);
        this.executor = new JobExecutor();
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

    private ImmutableList<Job> constructExecutorModel() {
        final MutableList<Job> jobs = Lists.mutable.empty();
        // TODO: Implement here

        return jobs.toImmutable();
    }

    @Override
    public List<Job> apply(final IRiemannClient riemannClient) {
        final JobBuilder<String,Integer> builder = new JobBuilder<String, Integer>()
                .named("String to Integer")
                .reader(new IterableRecordReader<>(List.of("1234")))
                .processor((RecordProcessor<String, Integer>) (final Record<String> record) -> new GenericRecord<>(
                        record.getHeader(),
                        Integer.valueOf(record.getPayload())
                ));

        final List<Future<JobReport>> reports = this.executor.submitAll(
            builder.build()
        );

        this.contextInjector.accept(this.contextBuilder);
        try {
            this.pipeline.withContext(RIEMANN_CLIENT_CTX_ATTRIBUTE, riemannClient)
                    .build()
                    .accept(this.initialMetrics);
        } catch (final Exception e) {
            LOGGER.error("Exception during metric processing pipeline invocation:", e);
        }
        return List.of(
                builder.build()
        );
    }

}
