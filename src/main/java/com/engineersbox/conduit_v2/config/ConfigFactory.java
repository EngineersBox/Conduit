package com.engineersbox.conduit_v2.config;

import com.typesafe.config.Config;

import java.io.File;

public abstract class ConfigFactory {

    private ConfigFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static ConduitConfig create(final String path) {
        final File file = new File(path);
        final Config typesafeConfig = com.typesafe.config.ConfigFactory.parseFile(file).resolve();
        return new ConduitConfig(typesafeConfig);
    }

}
