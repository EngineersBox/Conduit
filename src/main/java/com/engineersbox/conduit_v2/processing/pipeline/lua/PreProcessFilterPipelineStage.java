package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;

import java.util.Collection;

public class PreProcessFilterPipelineStage extends FilterPipelineStage<Metric> {

    private final LuaContextHandler contextHandler;
    private final ContextTransformer contextTransformer;

    public PreProcessFilterPipelineStage(final LuaContextHandler contextHandler,
                                         final ContextTransformer contextTransformer) {
        super("Pre-process Lua filter");
        this.contextHandler = contextHandler;
        this.contextTransformer = contextTransformer;
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
        final StageResult<Iterable<Metric>> result = super.invoke(previousResult);
        return new StageResult<>(
                StageResult.Type.SPLIT,
                result.result(),
                result.terminate()
        );
    }
}
