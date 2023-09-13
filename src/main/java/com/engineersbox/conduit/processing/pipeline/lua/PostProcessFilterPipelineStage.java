package com.engineersbox.conduit.processing.pipeline.lua;

import com.engineersbox.conduit.schema.extension.handler.ContextBuiltins;
import com.engineersbox.conduit.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.schema.extension.handler.LuaContextHandler;
import com.engineersbox.conduit.processing.event.EventSerialiser;
import com.engineersbox.conduit.processing.pipeline.PipelineStage;
import com.engineersbox.conduit.processing.pipeline.StageResult;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class PostProcessFilterPipelineStage extends PipelineStage<Object[], Proto.Event[]> {

    public static final String LUA_HANDLER_DEFINITION = "lua_post_process_handler_definition";
    public static final String LUA_HANDLER_NAME = "lua_post_process_handler_name";

    private final Function<String, LuaContextHandler> handlerRetriever;
    private final ContextTransformer.Builder contextBuilder;

    public PostProcessFilterPipelineStage(final Function<String, LuaContextHandler> handlerRetriever,
                                          final ContextTransformer.Builder contextBuilder) {
        super("Post-process Lua filter");
        this.handlerRetriever = handlerRetriever;
        this.contextBuilder = contextBuilder;
    }

    public boolean test(final EventBatch element) {
        final ImmutableMap<String, Object> extensions = element.getMetricExtensions();
        final String definition = getExtension(extensions, PostProcessFilterPipelineStage.LUA_HANDLER_DEFINITION);
        final String handlerName = getExtension(extensions, PostProcessFilterPipelineStage.LUA_HANDLER_NAME);
        if (definition == null || handlerName == null) {
            return true;
        }
        final LuaContextHandler handler = this.handlerRetriever.apply(definition);
        this.contextBuilder.withReadOnly(
                "events",
                element.getEvents(),
                EventSerialiser.class
        ).withTable("executionContext", ContextBuiltins.EXECUTION_CONTEXT);
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
    public StageResult<Proto.Event[]> invoke(final Object[] previousResult) {
        Stream<EventBatch> result = Arrays.stream(previousResult)
                .map((final Object obj) -> (EventBatch[]) obj)
                .flatMap(Arrays::stream);
        if (this.handlerRetriever != null) {
            result = result.filter(this::test);
        }
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                result.map(EventBatch::getEvents)
                        .flatMap(Arrays::stream)
                        .toArray(Proto.Event[]::new),
                false
        );
    }

    private String getExtension(final ImmutableMap<String, Object> extensions,
                                final String name) {
        final Object extension = extensions.get(name);
        if (extension == null) {
            return null;
        }
        if (extension instanceof String stringExtension) {
            return stringExtension;
        }
        return null;
    }

}
