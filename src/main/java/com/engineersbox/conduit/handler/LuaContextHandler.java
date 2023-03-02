package com.engineersbox.conduit.handler;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaContextHandler {

    private final String scriptPath;
    private LuaValue chunk;

    public LuaContextHandler(final String path) {
        this.scriptPath = path;
    }

    public Object invoke(final String target,
                         final LuaTable context) {
        final Globals globals = JsePlatform.standardGlobals();
        this.chunk = globals.loadfile(this.scriptPath);
        final Varargs result = this.chunk.invokemethod(
                target,
                context
        );
        return parseContextFromVarargs(result);
    }

    private Object parseContextFromVarargs(final Varargs args) {
        // TODO: Implement this
        return new Object();
    }

}
