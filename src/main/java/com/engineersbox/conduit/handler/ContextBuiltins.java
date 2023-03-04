package com.engineersbox.conduit.handler;

import org.luaj.vm2.*;

public final class ContextBuiltins {

    private ContextBuiltins() {
        throw new IllegalStateException("Static only class");
    }
    public static final LuaTable EXECUTION_CONTEXT = LuaTable.tableOf(
            new LuaValue[]{
                    LuaString.valueOf("shouldRun")
            },
            new LuaValue[]{
                    LuaValue.TRUE
            }
    );

    // These entries are saturated in a pipeline
    public static final LuaTable METRIC_INFO = LuaTable.tableOf(
            new LuaValue[]{
                    LuaString.valueOf("namespace"),
                    LuaString.valueOf("path"),
                    LuaString.valueOf("type")
            },
            new LuaValue[]{
                    LuaValue.NIL,
                    LuaValue.NIL,
                    LuaValue.NIL
            }
    );

    public static final LuaTable HANDLER_CONTEXT = LuaTable.tableOf(
            new LuaValue[]{
                    LuaString.valueOf("executionContext"),
                    LuaString.valueOf("metric")
            },
            new LuaValue[]{
                    EXECUTION_CONTEXT,
                    METRIC_INFO
            }
    );


}
