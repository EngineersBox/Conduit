package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextBuiltins;
import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import com.engineersbox.conduit_v2.processing.schema.metric.Metric;

import java.util.function.Function;

public class PreProcessFilterPipelineStage extends FilterPipelineStage<Metric> {

    public static final String FILTERED_COUNT_ATTRIBUTE = "pre_process_filtered_count";

    private final Function<String, LuaContextHandler> contextHandlerRetriever;
    private final ContextTransformer.Builder contextBuilder;
    private final boolean hasLuaHandlers;

    public PreProcessFilterPipelineStage(final Function<String, LuaContextHandler> contextHandlerRetriever,
                                         final ContextTransformer.Builder contextBuilder,
                                         final boolean hasLuaHandlers) {
        super("Pre-process Lua filter");
        this.contextHandlerRetriever = contextHandlerRetriever;
        this.contextBuilder = contextBuilder;
        this.hasLuaHandlers = hasLuaHandlers;
    }

    @Override
    public boolean test(final Metric metric) {
        final LuaContextHandler handler = this.contextHandlerRetriever.apply(metric.getNamespace());
        if (handler == null) {
            return true;
        }
        this.contextBuilder.withReadOnly(
                "metric",
                metric
                // TODO: MetricSerializer.class
        ).withTable("executionContext", ContextBuiltins.EXECUTION_CONTEXT);
        this.invoke(
                handler,
                this.contextBuilder.build().transform()
        );
        return this.contextHandler.getFromResult(
                new String[]{
                        "executionContext",
                        "shouldRun"
                },
                boolean.class
        );
    }

    @Override
    public StageResult<Iterable<Metric>> invoke(final Iterable<Metric> previousResult) {
        if (!this.hasLuaHandlers) {
            return new StageResult<>(
                    StageResult.Type.SPLIT,
                    previousResult,
                    false
            );
        }
        final StageResult<Iterable<Metric>> result =  super.invoke(previousResult);
        setContextAttribute(PreProcessFilterPipelineStage.FILTERED_COUNT_ATTRIBUTE, super.filteredCount);
        return new StageResult<>(
                StageResult.Type.SPLIT,
                result.result(),
                result.terminate()
        );
    }
}
