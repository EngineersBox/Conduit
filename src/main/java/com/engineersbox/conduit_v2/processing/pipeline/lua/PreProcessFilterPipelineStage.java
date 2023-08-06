package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextBuiltins;
import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import com.engineersbox.conduit_v2.processing.schema.metric.Metric;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.function.Function;

public class PreProcessFilterPipelineStage extends FilterPipelineStage<Metric> {

    public static final String FILTERED_COUNT_ATTRIBUTE = "pre_process_filtered_count";
    public static final String LUA_HANDLER_DEFINITIONS = "lua_pre_process_handler_definition";
    public static final String LUA_HANDLER_NAME = "lua_pre_process_handler_name";

    private final Function<String, LuaContextHandler> contextHandlerRetriever;
    private final ContextTransformer.Builder contextBuilder;

    public PreProcessFilterPipelineStage(final Function<String, LuaContextHandler> contextHandlerRetriever,
                                         final ContextTransformer.Builder contextBuilder) {
        super("Pre-process Lua filter");
        this.contextHandlerRetriever = contextHandlerRetriever;
        this.contextBuilder = contextBuilder;
    }

    @Override
    public boolean test(final Metric metric) {
        this.contextBuilder.withReadOnly(
                "metric",
                metric
                // TODO: MetricSerializer.class
        ).withTable("executionContext", ContextBuiltins.EXECUTION_CONTEXT);
        final ImmutableMap<String, Object> extensions = metric.getExtensions();
        if (!(extensions.get(LUA_HANDLER_DEFINITIONS) instanceof String handlerDefinition)) {
            return true;
        }
        if (!(extensions.get(LUA_HANDLER_NAME) instanceof String handlerName)) {
            return true;
        }
        final LuaContextHandler handler = this.contextHandlerRetriever.apply(handlerDefinition);
        handler.invoke(
                handlerName,
                this.contextBuilder.build().transform()
        );
        return handler.getFromResult(
                new String[]{
                        "executionContext",
                        "shouldRun"
                },
                boolean.class
        );
    }

    @Override
    public StageResult<Iterable<Metric>> invoke(final Iterable<Metric> previousResult) {
        if (this.contextHandlerRetriever == null) {
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
