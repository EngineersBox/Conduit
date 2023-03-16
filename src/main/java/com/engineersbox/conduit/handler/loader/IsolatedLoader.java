package com.engineersbox.conduit.handler.loader;

import com.engineersbox.conduit.handler.globals.GlobalsProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class IsolatedLoader {

    private final File fsLocation;
    private final HandlerFileType type;
    private LuaFunction lib;

    public IsolatedLoader(final String path) {
        this.fsLocation = new File(path).getAbsoluteFile();
        validateFsLocation();
        this.type = HandlerFileType.fromFile(this.fsLocation);
    }

    private void validateFsLocation() {
        if (!this.fsLocation.isFile()) {
            throw new IllegalStateException(String.format(
                    "Source location is not a file: %s",
                    this.fsLocation.getPath()
            ));
        } else if (!this.fsLocation.exists()) {
            throw new IllegalStateException(String.format(
                    "Unable to find file at path: %s",
                    this.fsLocation.getPath()
            ));
        }
    }

    public void load(final GlobalsProvider provider) {
        this.lib = switch (this.type) {
            case CLASS -> loadClass(provider);
            case LUA -> loadScript(provider);
        };
    }

    private LuaFunction loadClass(final GlobalsProvider provider) {
        final URL parent;
        try {
            parent = this.fsLocation.getParentFile().toURI().toURL();
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try (final URLClassLoader loader = new URLClassLoader(new URL[]{parent})) {
            final String className = StringUtils.substringBeforeLast(
                    this.fsLocation.getName(),
                    "."
            );
            final Class<?> handlerClass = loader.loadClass(className);
            final Object instance = ConstructorUtils.invokeConstructor(handlerClass);
            if (instance instanceof LuaFunction luaFuncInstance) {
                luaFuncInstance.initupvalue1(provider.getGlobals());
                return luaFuncInstance;
            }
            throw new IllegalStateException(String.format(
                    "Loaded class \"%s\" does not inherit from LuaFunction",
                    handlerClass.getName()
            ));
        } catch (final IOException
                | ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private LuaFunction loadScript(final GlobalsProvider provider) {
        final LuaValue libResult = provider.getGlobals().load(this.fsLocation.getAbsolutePath());
        if (libResult instanceof LuaFunction funcLibResult) {
            return funcLibResult;
        }
        throw new IllegalStateException(String.format(
                "Expected LuaFunction result from loaded library %s, got %s",
                this.fsLocation.getPath(),
                libResult.typename()
        ));
    }

    public LuaTable getLib() {
        final LuaValue result = this.lib.call();
        if (result instanceof LuaTable tableResult) {
            return tableResult;
        }
        throw new IllegalStateException(String.format(
                "Expected LuaTable result type, got %s",
                result.typename()
        ));
    }

}
