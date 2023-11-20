package com.engineersbox.conduit.core.processing.event;

import io.riemann.riemann.Proto;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.function.Function;
import java.util.function.Predicate;

public class EventDeserialiser implements Function<LuaValue, Proto.Event> {

    private final Proto.Event template;

    public EventDeserialiser(final Proto.Event template) {
        this.template = template;
    }

    @Override
    public Proto.Event apply(final LuaValue luaValue) {
        if (luaValue == null || !luaValue.istable()) {
            throw new IllegalStateException("Attempted to deserialise non-table Proto.Event from LuaValue");
        }
        final LuaTable table = luaValue.checktable();
        final Proto.Event.Builder builder = this.template.toBuilder();
        setField(
                table.get("state"),
                LuaValue::isstring,
                LuaValue::tojstring,
                builder::setState
        );
        setField(
                table.get("service"),
                LuaValue::isstring,
                LuaValue::tojstring,
                builder::setService
        );
        setField(
                table.get("host"),
                LuaValue::isstring,
                LuaValue::tojstring,
                builder::setHost
        );
        setField(
                table.get("description"),
                LuaValue::isstring,
                LuaValue::tojstring,
                builder::setDescription
        );
        setField(
                table.get("tags"),
                LuaValue::istable,
                (final LuaValue value) -> {
                    builder.clearTags();
                    final LuaTable tags = value.checktable();
                    final LuaValue[] keys = tags.keys();
                    final LuaValue[] values = new LuaValue[tags.keyCount()];
                    for (int i = 0; i < tags.keyCount(); i++) {
                        values[i] = tags.get(keys[i]);
                    }
                    return values;
                },
                (final LuaValue[] tags) -> {
                    for (final LuaValue value : tags) {
                        if (value != null) {
                            builder.addTags(value.tostring().tojstring());
                        }
                    }
                    return builder;
                }
        );
        setField(
                table.get("attributes"),
                LuaValue::istable,
                (final LuaValue value) -> {
                    builder.clearAttributes();
                    final LuaTable attributes = value.checktable();
                    final LuaValue[] keys = attributes.keys();
                    final Proto.Attribute[] values = new Proto.Attribute[attributes.keyCount()];
                    for (int i = 0; i < attributes.keyCount(); i++) {
                        final Proto.Attribute.Builder attrBuilder = Proto.Attribute.newBuilder();
                        if (keys[i] != null) {
                            attrBuilder.setKey(keys[i].tostring().tojstring());
                        }
                        final LuaValue attrValue = attributes.get(keys[i]);
                        if (attrValue != null && attrValue.isstring()) {
                            attrBuilder.setValue(attrValue.tostring().tojstring());
                        }
                        values[i] = attrBuilder.build();
                    }
                    return values;
                },
                (final Proto.Attribute[] attributes) -> {
                    for (final Proto.Attribute attribute : attributes) {
                        builder.addAttributes(attribute);
                    }
                    return builder;
                }
        );
        setField(
                table.get("ttl"),
                LuaValue::isnumber,
                LuaValue::tofloat,
                builder::setTtl
        );
        setField(
                table.get("timeMicros"),
                LuaValue::islong,
                LuaValue::tolong,
                builder::setTimeMicros
        );
        setField(
                table.get("metricSint64"),
                LuaValue::islong,
                LuaValue::tolong,
                builder::setMetricSint64
        );
        setField(
                table.get("metricD"),
                LuaValue::isnumber,
                LuaValue::todouble,
                builder::setMetricD
        );
        setField(
                table.get("metricF"),
                LuaValue::isnumber,
                LuaValue::tofloat,
                builder::setMetricF
        );
        return builder.build();
    }

    @SuppressWarnings("ReturnValueIgnored")
    private <T> void setField(final LuaValue value,
                              final Predicate<LuaValue> predicate,
                              final Function<LuaValue, T> converter,
                              final Function<T, Proto.Event.Builder> saturator) {
        if (value != null && predicate.test(value)) {
            saturator.apply(converter.apply(value));
        }
    }

}
