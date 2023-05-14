package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import io.riemann.riemann.Proto;

public class PostProcessFilterPipelineStage extends FilterPipelineStage<Proto.Event[]> {

    private final LuaContextHandler contextHandler;
    private final ContextTransformer contextTransformer;

    public PostProcessFilterPipelineStage(final LuaContextHandler contextHandler,
                                          final ContextTransformer contextTransformer) {
        super("Pre-process Lua filter");
        this.contextHandler = contextHandler;
        this.contextTransformer = contextTransformer;
    }

    @Override
    public boolean test(final Proto.Event[] element) {
        final Object handlerObj = getContextAttribute(HandlerSaturationPipelineStage.LUA_HANDLER_PREFIX + "post_process");
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

}
