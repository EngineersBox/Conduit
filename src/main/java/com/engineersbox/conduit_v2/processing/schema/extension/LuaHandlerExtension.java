package com.engineersbox.conduit_v2.processing.schema.extension;

import com.engineersbox.conduit.handler.LuaContextHandler;
import com.engineersbox.conduit.handler.LuaStdoutSink;
import com.engineersbox.conduit.handler.globals.GlobalsProvider;
import com.engineersbox.conduit.handler.globals.LazyLoadedGlobalsProvider;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.eclipse.collections.api.map.ImmutableMap;
import org.luaj.vm2.Globals;
import org.slf4j.event.Level;

import java.io.InputStream;
import java.nio.file.Path;


public class LuaHandlerExtension implements Extension {

    static GlobalsProvider GLOBALS_PROVIDER = new LazyLoadedGlobalsProvider(
            LuaHandlerExtension::configureGlobals,
            false
    );
    private static String LUA_CONTEXT_LOGGER_NAME = "LuaHandlerExtension";
    private static Level LUA_CONTEXT_LOGGER_LEVEL = Level.INFO;

    @JsonProperty("lua_handlers_definition")
    @JsonAlias("luaHandlersDefinition")
    private Path handlersDefinition;
    @JsonProperty("lua_pre_process_handler_definitions")
    @JsonAlias("luaPreProcessHandlerDefinitions")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaPreProcessHandlerDefinitions;
    @JsonProperty("lua_adapter_handler_definitions")
    @JsonAlias("luaAdapterHandlers")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaAdapterHandlerDefinitions;
    @JsonProperty("lua_post_process_handler_definitions")
    @JsonAlias("luaPostProcessHandlerDefinitions")
    @JsonDeserialize(contentUsing = LuaContextHandlerDeserializer.class)
    private ImmutableMap<String, LuaContextHandler> luaPostProcessHandlerDefinitions;

    public Path getHandlersDefinition() {
        return this.handlersDefinition;
    }

    public LuaContextHandler getPreProcessHandleDefinition(final String name) {
        return this.luaPreProcessHandlerDefinitions.get(name);
    }

    public LuaContextHandler getAdapterHandleDefinition(final String name) {
        return this.luaAdapterHandlerDefinitions.get(name);
    }

    public LuaContextHandler getPostProcessHandleDefinition(final String name) {
        return this.luaPostProcessHandlerDefinitions.get(name);
    }

    @Override
    public String name() {
        return "lua_handlers";
    }

    @Override
    public TypeReference<? extends Extension> targetType() {
        return new TypeReference<LuaHandlerExtension>() {};
    }

    @Override
    public InputStream schemaPatchStream() {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schemas/lua/lua_handlers_schema_patch.json");
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
