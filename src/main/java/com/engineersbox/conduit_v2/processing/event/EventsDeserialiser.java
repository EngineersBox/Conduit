package com.engineersbox.conduit_v2.processing.event;

import io.riemann.riemann.Proto;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.function.Function;

public class EventsDeserialiser implements Function<LuaValue, Proto.Event[]> {

    private final Proto.Event template;

    public EventsDeserialiser(final Proto.Event template) {
        this.template = template;
    }

    @Override
    public Proto.Event[] apply(final LuaValue luaValue) {
        final EventDeserialiser deserialiser = new EventDeserialiser(this.template);
        if (!luaValue.istable()) {
            return new Proto.Event[]{ deserialiser.apply(luaValue) };
        }
        final LuaTable table = luaValue.checktable();
        final LuaValue[] keys = table.keys();
        final Proto.Event[] events = new Proto.Event[keys.length];
        for (int i = 0; i < events.length; i++) {
            events[i] = deserialiser.apply(keys[i]);
        }
        return events;
    }

}
