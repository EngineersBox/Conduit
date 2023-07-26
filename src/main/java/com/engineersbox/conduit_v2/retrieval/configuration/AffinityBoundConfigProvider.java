package com.engineersbox.conduit_v2.retrieval.configuration;

import com.jayway.jsonpath.Configuration;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AffinityBoundConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityBoundConfigProvider.class);
    private static final MutableMap<Long, Configuration> CONFIGURATIONS = Maps.mutable.of();
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock(true);
    private static final Lock READ_LOCK = RW_LOCK.readLock();
    private static final Lock WRITE_LOCK = RW_LOCK.writeLock();

    public static synchronized void bindConfiguration(final long affinityId,
                                                      final Configuration config) {
        WRITE_LOCK.lock();
        try {
            final Configuration previousConfig = CONFIGURATIONS.put(affinityId, config);
            if (previousConfig == null) {
                LOGGER.warn(
                        "Overwritten previous JsonPath configuration [Affinity ID: {}]",
                        affinityId
                );
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    public static synchronized Configuration getConfiguration(final long affinityId) {
        READ_LOCK.lock();
        try {
            return CONFIGURATIONS.get(affinityId);
        } finally {
            READ_LOCK.unlock();
        }
    }

}
