package com.engineersbox.conduit.schema.extension.handler.globals;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class LazyLoadedGlobalsProvider implements GlobalsProvider {

    private final Supplier<Globals> globalsSupplier;
    private final Function<Globals, Globals> globalsConfigurator;
    private Globals globals;

    public LazyLoadedGlobalsProvider(@Nonnull final Function<Globals, Globals> globalsConfigurator,
                                     final boolean debug) {
        this.globalsSupplier = debug ? JsePlatform::debugGlobals : JsePlatform::standardGlobals;
        this.globalsConfigurator = globalsConfigurator;
        this.globals = null;
    }

    public LazyLoadedGlobalsProvider(final boolean debug) {
        this(Function.identity(), debug);
    }

    @Override
    public Globals getGlobals() {
        if (this.globals == null) {
            this.globals = this.globalsConfigurator.apply(this.globalsSupplier.get());
        }
        return this.globals;
    }
}
