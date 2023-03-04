package com.engineersbox.conduit.handler;

import org.apache.commons.lang3.ArrayUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class LuaContextHandler {

    private static final Map<Class<?>, Function<LuaValue, ?>> CONVERTERS = Map.of(
            String.class, LuaValue::toString,
            char.class, LuaValue::tochar,
            boolean.class, LuaValue::toboolean,
            byte.class, LuaValue::tobyte,
            short.class, LuaValue::toshort,
            int.class, LuaValue::toint,
            long.class, LuaValue::tolong,
            float.class, LuaValue::tofloat,
            double.class, LuaValue::todouble
    );

    private final Globals globals;
    private final String scriptPath;
    private LuaTable result;

    public LuaContextHandler(final String path,
                             final Globals globals) {
        this.globals = globals;
        this.scriptPath = path;
    }

    public void invoke(final String target,
                       final LuaTable context) {
//        final Globals globals = JsePlatform.standardGlobals();
        final LuaValue chunk = this.globals.loadfile(this.scriptPath);
        final Varargs result = chunk.invokemethod(
                target,
                context
        );
        final LuaValue arg0 = result.arg(0);
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
}
