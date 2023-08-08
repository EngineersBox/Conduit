package com.engineersbox.conduit_v2.config;

import com.typesafe.config.Config;

import java.io.File;

public abstract class ConfigFactory {

    private ConfigFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    public static ConduitConfig create(final String path) {
        final File file = new File(path);
        final Config typesafeConfig = com.typesafe.config.ConfigFactory.parseFile(file).resolve();
        return new ConduitConfig(typesafeConfig);
    }

    public static ConduitConfig createDefault() {
        return new ConduitConfig(com.typesafe.config.ConfigFactory.defaultApplication());
    }

}
