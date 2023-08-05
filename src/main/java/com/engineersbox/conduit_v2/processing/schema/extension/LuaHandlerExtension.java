package com.engineersbox.conduit_v2.processing.schema.extension;

import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.handler.LuaStdoutSink;
import com.engineersbox.conduit.handler.globals.GlobalsProvider;
import com.engineersbox.conduit.handler.globals.LazyLoadedGlobalsProvider;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.collections.api.map.ImmutableMap;
import org.luaj.vm2.Globals;
import org.slf4j.event.Level;

import java.nio.file.Path;

public class LuaHandlerExtension {

    static GlobalsProvider GLOBALS_PROVIDER = new LazyLoadedGlobalsProvider(
            LuaHandlerExtension::configureGlobals,
            false
    );
    private static String LUA_CONTEXT_LOGGER_NAME = "LuaHandlerExtension";
    private static Level LUA_CONTEXT_LOGGER_LEVEL = Level.INFO;

    @JsonProperty("handlers_definition")
    @JsonAlias("handlersDefinition")
    private Path handlersDefinition;
    @JsonProperty("pre_process_handlers")
    @JsonAlias("preProcessHandlers")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaPreProcessHandlers;
    @JsonProperty("adapter_handlers")
    @JsonAlias("adapterHandlers")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaAdapterHandlers;
    @JsonProperty("post_process_handlers")
    @JsonAlias("postProcessHandlers")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaPostProcessHandlers;

    public Path getHandlersDefinition() {
        return this.handlersDefinition;
    }

    public LuaContextHandler getPreProcessHandler(final String name) {
        return this.luaPreProcessHandlers.get(name);
    }

    public LuaContextHandler getAdapterHandler(final String name) {
        return this.luaAdapterHandlers.get(name);
    }

    public LuaContextHandler getPostProcessHandler(final String name) {
        return this.luaPostProcessHandlers.get(name);
    }

    public static void setLuaContextLoggerName(final String name) {
        LUA_CONTEXT_LOGGER_NAME = name;
    }

    public static void setLuaContextLoggerLevel(final Level level) {
        LUA_CONTEXT_LOGGER_LEVEL = level;
    }

    private static Globals configureGlobals(final Globals standard) {
        standard.STDOUT = LuaStdoutSink.createSlf4j(
                LUA_CONTEXT_LOGGER_NAME,
                LUA_CONTEXT_LOGGER_LEVEL
        );
        return standard;
    }

}
