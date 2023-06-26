package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;

public class PreProcessFilterPipelineStage extends FilterPipelineStage<Metric> {

    private final LuaContextHandler contextHandler;
    private final ContextTransformer contextTransformer;
    private final boolean hasLuaHandlers;

    public PreProcessFilterPipelineStage(final LuaContextHandler contextHandler,
                                         final ContextTransformer contextTransformer,
                                         final boolean hasLuaHandlers) {
        super("Pre-process Lua filter");
        this.contextHandler = contextHandler;
        this.contextTransformer = contextTransformer;
        this.hasLuaHandlers = hasLuaHandlers;
    }

    @Override
    public boolean test(final Metric metric) {
        final Object handlerObj = getContextAttribute(HandlerSaturationPipelineStage.LUA_HANDLER_PREFIX + "pre_process");
        if (!(handlerObj instanceof String handler)) {
            return true;
        }
        this.contextHandler.invoke(
                handler,
                this.contextTransformer.transform()
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
        return new StageResult<>(
                StageResult.Type.SPLIT,
                result.result(),
                result.terminate()
        );
    }
}
