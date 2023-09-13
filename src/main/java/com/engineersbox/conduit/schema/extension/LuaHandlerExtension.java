package com.engineersbox.conduit.schema.extension;

import com.engineersbox.conduit.schema.extension.handler.ContextBuiltins;
import com.engineersbox.conduit.schema.extension.handler.LuaContextHandler;
import com.engineersbox.conduit.schema.extension.handler.LuaStdoutSink;
import com.engineersbox.conduit.schema.extension.handler.globals.GlobalsProvider;
import com.engineersbox.conduit.schema.extension.handler.globals.LazyLoadedGlobalsProvider;
import com.engineersbox.conduit.schema.metric.Metric;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.eclipse.collections.api.map.ImmutableMap;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.slf4j.event.Level;

import java.io.InputStream;

public class LuaHandlerExtension {

    public static final GlobalsProvider GLOBALS_PROVIDER = new LazyLoadedGlobalsProvider(
            LuaHandlerExtension::configureGlobals,
            false
    );
    public static final String SCHEMA_EXTENSION_FIELD_NAME = "lua_handlers";
    private static String LUA_CONTEXT_LOGGER_NAME = "LuaHandlerExtension";
    private static Level LUA_CONTEXT_LOGGER_LEVEL = Level.INFO;

    @JsonProperty("lua_pre_process_handler_definitions")
    @JsonAlias("luaPreProcessHandlerDefinitions")
    private ImmutableMap<String, LuaContextHandler> luaPreProcessHandlerDefinitions;
    @JsonProperty("lua_adapter_handler_definitions")
    @JsonAlias("luaAdapterHandlers")
    private ImmutableMap<String, LuaContextHandler> luaAdapterHandlerDefinitions;
    @JsonProperty("lua_post_process_handler_definitions")
    @JsonAlias("luaPostProcessHandlerDefinitions")
    private ImmutableMap<String, LuaContextHandler> luaPostProcessHandlerDefinitions;

    public LuaContextHandler getPreProcessHandleDefinition(final String name) {
        return this.luaPreProcessHandlerDefinitions.get(name);
    }

    public LuaContextHandler getAdapterHandleDefinition(final String name) {
        return this.luaAdapterHandlerDefinitions.get(name);
    }

    public LuaContextHandler getPostProcessHandleDefinition(final String name) {
        return this.luaPostProcessHandlerDefinitions.get(name);
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

    public static LuaTable constructContextAttributes(final Metric metric) {
        final LuaTable ctx = ContextBuiltins.METRIC_INFO;
        ctx.set("namespace", metric.getNamespace());
        ctx.set("path", metric.getPath());
        ctx.set("type", TypeUtils.toString(metric.getStructure().intoConcrete().getType()));
        return ctx;
    }

    public static ExtensionMetadata getExtensionMetadata() {
        return new LuaHandlerExtensionMetadata();
    }

    private static class LuaHandlerExtensionMetadata implements ExtensionMetadata {

        private LuaHandlerExtensionMetadata() {}

        @Override
        public String name() {
            return LuaHandlerExtension.SCHEMA_EXTENSION_FIELD_NAME;
        }

        @Override
        public TypeReference<?> targetType() {
            return new TypeReference<LuaHandlerExtension>() {};
        }

        @Override
        public InputStream schemaPatchStream() {
            return Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("schemas/lua/lua_handlers_schema_patch.json");
        }

    }

}
