package com.engineersbox.conduit.core.retrieval.configuration;

import com.jayway.jsonpath.Configuration;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class AffinityBoundConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityBoundConfigProvider.class);
    private static final ConcurrentMutableMap<Long, Configuration> CONFIGURATIONS = ConcurrentHashMap.newMap();
    private static final AtomicReference<Configuration> DEFAULT_CONFIGURATION = new AtomicReference<>();

    public static void bindDefaultConfiguration(final Configuration config) {
        DEFAULT_CONFIGURATION.set(config);
    }

    public static void removeDefaultConfiguration() {
        DEFAULT_CONFIGURATION.set(null);
    }

    public static void bindConfiguration(final long affinityId,
                                                      final Configuration config) {
        final Configuration previousConfig = CONFIGURATIONS.put(affinityId, config);
        if (previousConfig == null) {
            LOGGER.warn(
                    "Overwritten previous JsonPath configuration [Affinity ID: {}]",
                    affinityId
            );
        }
    }

    public static Configuration getConfiguration(final long affinityId) {
        return CONFIGURATIONS.getOrDefault(
                affinityId,
                DEFAULT_CONFIGURATION.get()
        );
    }

}
