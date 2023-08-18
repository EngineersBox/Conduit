package com.engineersbox.conduit.handler;

import com.engineersbox.conduit.handler.globals.GlobalsProvider;
import com.engineersbox.conduit.handler.loader.IsolatedLoader;
import com.engineersbox.conduit_v2.schema.extension.LuaContextHandlerDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@JsonDeserialize(using = LuaContextHandlerDeserializer.class)
public class LuaContextHandler {

    private static final ImmutableMap<Class<?>, Function<LuaValue, ?>> CONVERTERS = Maps.immutable.ofAll(Map.of(
            String.class, LuaValue::toString,
            char.class, LuaValue::tochar,
            boolean.class, LuaValue::toboolean,
            byte.class, LuaValue::tobyte,
            short.class, LuaValue::toshort,
            int.class, LuaValue::toint,
            long.class, LuaValue::tolong,
            float.class, LuaValue::tofloat,
            double.class, LuaValue::todouble
    ));

    private final IsolatedLoader loader;
    private LuaTable result;

    @JsonCreator
    public LuaContextHandler(final String path,
                             final GlobalsProvider globalsProvider) {
        this.loader = new IsolatedLoader(path);
        this.loader.load(globalsProvider);
    }

    public void invoke(final String target,
                       final LuaTable context) {
        final LuaTable table = this.loader.getLib();
        final Varargs result = table.invokemethod(
                target,
                LuaValue.varargsOf(new LuaValue[]{context})
        );
        final LuaValue arg0 = result.arg1();
        if (!arg0.istable()) {
            throw new IllegalStateException(String.format(
                    "Expected \"%s\" handler method to return a table, got %s",
                    target,
                    arg0.typename()
            ));
        }
        this.result = (LuaTable) arg0;
    }

    private <T> T getFromResult0(final String[] target,
                                 final Function<LuaValue, T> converter) {
        if (ArrayUtils.isEmpty(target)) {
            throw new IllegalArgumentException("Target cannot be null or empty");
        }
        LuaValue current = this.result;
        for (int i = 0; i < target.length; i++) {
            if (i != (target.length - 1) && !current.istable()) {
                throw new IllegalStateException(String.format(
                        "Expected table at \"%s\", got %s",
                        Arrays.toString(ArrayUtils.subarray(target, 0, i + 1)),
                        current.typename()
                ));
            }
            try {
                current = current.get(Integer.parseInt(target[i]));
            } catch (final NumberFormatException ignored) {
                current = current.get(target[i]);
            }
        }
        return converter.apply(current);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getFromResult(final String[] target,
                               final Class<T> type) {
        final Function<LuaValue, ?> converter = CONVERTERS.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("Type not supported: " + type.getName());
        }
        return (T) getFromResult0(target, converter);
    }

    public <T> T getFromResult(final String[] target,
                               final Function<LuaValue, T> converter) {
        return getFromResult0(target, converter);
    }

}
