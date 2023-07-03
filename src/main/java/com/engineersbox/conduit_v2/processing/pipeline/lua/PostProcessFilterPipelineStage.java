package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import io.riemann.riemann.Proto;

import java.util.Arrays;
import java.util.stream.Stream;

public class PostProcessFilterPipelineStage extends PipelineStage<Object[], Proto.Event[]> {

    private final LuaContextHandler contextHandler;
    private final ContextTransformer contextTransformer;
    private final boolean hasLuaHandlers;

    public PostProcessFilterPipelineStage(final LuaContextHandler contextHandler,
                                          final ContextTransformer contextTransformer,
                                          final boolean hasLuaHandlers) {
        super("Post-process Lua filter");
        this.contextHandler = contextHandler;
        this.contextTransformer = contextTransformer;
        this.hasLuaHandlers = hasLuaHandlers;
    }

    public boolean test(final Proto.Event element) {
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

    @Override
    public StageResult<Proto.Event[]> invoke(final Object[] previousResult) {
        Stream<Proto.Event> result = Arrays.stream(previousResult)
                .map((final Object obj) -> (Proto.Event[]) obj)
                .flatMap(Arrays::stream);
        if (this.hasLuaHandlers) {
            result = result.filter(this::test);
        }
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                result.toArray(Proto.Event[]::new),
                false
        );
    }
}
