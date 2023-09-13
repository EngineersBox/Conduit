package com.engineersbox.conduit.processing.pipeline.lua;

import com.engineersbox.conduit.schema.extension.handler.ContextBuiltins;
import com.engineersbox.conduit.schema.extension.handler.ContextTransformer;
import com.engineersbox.conduit.schema.extension.handler.LuaContextHandler;
import com.engineersbox.conduit.processing.event.EventSerialiser;
import com.engineersbox.conduit.processing.event.EventsDeserialiser;
import com.engineersbox.conduit.processing.pipeline.PipelineStage;
import com.engineersbox.conduit.processing.pipeline.StageResult;
import io.riemann.riemann.Proto;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Arrays;
import java.util.function.Function;

public class AdapterProcessPipelineStage extends PipelineStage<Object[], Object[][]> {

    public static final String LUA_HANDLER_DEFINITION = "lua_adapter_handler_definition";
    public static final String LUA_HANDLER_NAME = "lua_adapter_handler_name";

    private final ContextTransformer.Builder contextBuilder;
    private final Function<String, LuaContextHandler> handlerRetriever;
    private final Proto.Event eventTemplate;

    public AdapterProcessPipelineStage(final Function<String, LuaContextHandler> handlerRetriever,
                                       final ContextTransformer.Builder contextBuilder,
                                       final Proto.Event eventTemplate) {
        super("Adapter Lua handler");
        this.handlerRetriever = handlerRetriever;
        this.contextBuilder = contextBuilder;
        this.eventTemplate = eventTemplate;
    }

    @Override
    public StageResult<Object[][]> invoke(final Object[] events) {
        final EventBatch[] eventsStream = Arrays.stream(events)
                .map((final Object obj) -> (EventBatch) obj)
                .map(this::processEvents)
                .toArray(EventBatch[]::new);
        final Object[][] result = new Object[1][];
        result[0] = eventsStream;
        return new StageResult<>(
                StageResult.Type.SINGLETON,
                result,
                false
        );
    }

    private EventBatch processEvents(final EventBatch batch) {
        final ImmutableMap<String, Object> extensions = batch.getMetricExtensions();
        final String definition = getExtension(extensions, AdapterProcessPipelineStage.LUA_HANDLER_DEFINITION);
        final String handlerName = getExtension(extensions, AdapterProcessPipelineStage.LUA_HANDLER_NAME);
        if (definition == null || handlerName == null) {
            return batch;
        }
        final LuaContextHandler handler = this.handlerRetriever.apply(definition);
        this.contextBuilder.withReadOnly(
                "events",
                batch.getEvents(),
                EventSerialiser.class
        ).withTable("executionContext", ContextBuiltins.EXECUTION_CONTEXT);
        handler.invoke(
                handlerName,
                this.contextBuilder.build().transform()
        );
        final Proto.Event[] newEvents = handler.getFromResult(
                new String[]{"events"},
                new EventsDeserialiser(this.eventTemplate)
        );
        return new EventBatch(
                newEvents,
                extensions
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
