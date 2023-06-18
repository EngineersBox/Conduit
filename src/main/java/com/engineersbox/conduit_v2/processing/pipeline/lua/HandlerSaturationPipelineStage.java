package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.schema.metric.Metric;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;

public class HandlerSaturationPipelineStage extends PipelineStage<Metric, Metric> {

    static final String LUA_HANDLER_PREFIX = "luaHandler_";

    public HandlerSaturationPipelineStage() {
        super("Handlers saturation");
    }

    @Override
    public Metric invoke(final Metric previousResult) {
        previousResult.getHandlers().forEachKeyValue((final String name, final String handler) ->
                setContextAttribute(LUA_HANDLER_PREFIX + name, handler)
        );
        return previousResult;
    }

}
