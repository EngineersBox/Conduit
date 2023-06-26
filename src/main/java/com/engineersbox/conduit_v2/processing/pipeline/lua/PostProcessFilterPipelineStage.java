package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.FilterPipelineStage;
import io.riemann.riemann.Proto;
import org.eclipse.collections.impl.collector.Collectors2;

import java.util.Arrays;

// FIXME: This expects an Iterable<Proto.Event[]> but is receiving Proto.Event[]
public class PostProcessFilterPipelineStage extends PipelineStage<Object[], Proto.Event[]> {

    private final LuaContextHandler contextHandler;
    private final ContextTransformer contextTransformer;

    public PostProcessFilterPipelineStage(final LuaContextHandler contextHandler,
                                          final ContextTransformer contextTransformer) {
        super("Pre-process Lua filter");
        this.contextHandler = contextHandler;
        this.contextTransformer = contextTransformer;
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
        final Proto.Event[] result = Arrays.stream(previousResult)
                .map((final Object obj) -> (Proto.Event[]) obj)
                .flatMap(Arrays::stream)
                .filter(this::test)
                .toArray(Proto.Event[]::new);
        return new StageResult<>(
                StageResult.Type.SINGLE,
                result,
                false
        );
    }
}
