package com.engineersbox.conduit_v2.processing.pipeline.lua;

import com.engineersbox.conduit.handler.ContextTransformer;
import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit_v2.processing.event.EventSerialiser;
import com.engineersbox.conduit_v2.processing.event.EventsDeserialiser;
import com.engineersbox.conduit_v2.processing.pipeline.PipelineStage;
import com.engineersbox.conduit_v2.processing.pipeline.StageResult;
import com.engineersbox.conduit_v2.processing.pipeline.core.ProcessPipelineStage;
import com.engineersbox.conduit_v2.processing.task.MetricProcessingTask;
import io.riemann.riemann.Proto;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.utility.Iterate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AdapterProcessPipelineStage extends PipelineStage<Object[], Object[][]> {

    private final ContextTransformer.Builder contextBuilder;
    private final LuaContextHandler contextHandler;
    private final ContextTransformer transformer;
    private final Proto.Event eventTemplate;

    public AdapterProcessPipelineStage(final ContextTransformer.Builder contextBuilder,
                                       final LuaContextHandler contextHandler,
                                       final ContextTransformer transformer,
                                       final Proto.Event eventTemplate) {
        super("Adapter Lua handler");
        this.contextBuilder = contextBuilder;
        this.contextHandler = contextHandler;
        this.transformer = transformer;
        this.eventTemplate = eventTemplate;
    }

    @Override
    public StageResult<Object[][]> invoke(final Object[] events) {
        final Object handlerObj = getContextAttribute(HandlerSaturationPipelineStage.LUA_HANDLER_PREFIX + "adapter");
        final Proto.Event[] eventsStream = Arrays.stream(events)
                .map((final Object obj) -> (Proto.Event[]) obj)
                .flatMap(Arrays::stream)
                .toArray(Proto.Event[]::new);
        if (!(handlerObj instanceof String handler)) {
            final Object[][] result = new Object[1][];
            result[0] = eventsStream;
            return new StageResult<>(
                    StageResult.Type.SINGLE,
                    result,
                    false
            );
        }
        this.contextBuilder.withReadOnly(
                "events",
                eventsStream,
                EventSerialiser.class
        );
        this.contextHandler.invoke(
                handler,
                this.transformer.transform()
        );
        final Proto.Event[] finalEvents = this.contextHandler.getFromResult(
                new String[]{
                        "events"
                },
                new EventsDeserialiser(this.eventTemplate)
        );
        final Object[][] result = new Object[1][];
        result[0] = finalEvents;
        return new StageResult<>(
                StageResult.Type.SINGLE,
                result,
                false
        );
    }

}
