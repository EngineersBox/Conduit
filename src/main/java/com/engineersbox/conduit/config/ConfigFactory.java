package com.engineersbox.conduit.config;

import com.typesafe.config.Config;

import java.io.File;
import java.nio.file.Path;

public abstract class ConfigFactory {

    private ConfigFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    public static ConduitConfig load(final Path path) {
        final File file = path.toFile();
        final Config typesafeConfig = com.typesafe.config.ConfigFactory.parseFile(file).resolve();
        return new ConduitConfig(typesafeConfig);
    }

    public static ConduitConfig create(final String literal) {
        final Config typesafeConfig = com.typesafe.config.ConfigFactory.parseString(literal).resolve();
        return new ConduitConfig(typesafeConfig);
    }

    public static ConduitConfig createDefault() {
        return new ConduitConfig(com.typesafe.config.ConfigFactory.defaultApplication());
    }

}
