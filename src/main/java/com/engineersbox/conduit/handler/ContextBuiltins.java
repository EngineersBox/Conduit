package com.engineersbox.conduit.handler;

import org.luaj.vm2.*;

public final class ContextBuiltins {

    private ContextBuiltins() {
        throw new IllegalStateException("Static only class");
    }
    public static final LuaTable EXECUTION_CONTEXT = LuaTable.tableOf(new LuaValue[]{
            LuaString.valueOf("shouldRun"), LuaValue.TRUE
    });

    // These entries are saturated in a pipeline
    public static final LuaTable METRIC_INFO = LuaTable.tableOf(new LuaValue[]{
            LuaString.valueOf("namespace"), LuaValue.NIL,
            LuaString.valueOf("path"), LuaValue.NIL,
            LuaString.valueOf("type"), LuaValue.NIL
    });

}
