package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.schema.Metric;
import org.eclipse.collections.api.RichIterable;

public class HandlerSaturationPipelineStage extends PipelineStage<RichIterable<Metric>, RichIterable<Metric>> {

    static final String LUA_HANDLER_PREFIX = "luaHandler_";

    public HandlerSaturationPipelineStage() {
        super("Handlers saturation");
    }

    @Override
    public StageResult<RichIterable<Metric>> invoke(final RichIterable<Metric> previousResults) {
        previousResults.forEach((final Metric previousResult) -> previousResult.getHandlers().forEachKeyValue((final String name, final String handler) ->
                setContextAttribute(LUA_HANDLER_PREFIX + name, handler)
        ));
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                previousResults,
                false
        );
    }

}
