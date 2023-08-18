package com.engineersbox.conduit_v2.schema.json.path;

import com.jayway.jsonpath.spi.cache.Cache;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AffinityCacheProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityCacheProvider.class);
    private static final MutableMap<Long, Cache> CACHES = Maps.mutable.of();
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock(true);
    private static final Lock READ_LOCK = RW_LOCK.readLock();
    private static final Lock WRITE_LOCK = RW_LOCK.writeLock();

    public static void bindCache(final long affinityId, final Cache cache) {
        WRITE_LOCK.lock();
        try {
            final Cache previousCache = CACHES.put(affinityId, cache);
            if (previousCache != null) {
                LOGGER.warn(
                        "Overwritten previous JsonPath cache registration [Affinity ID: {}] [Previous: {}] [New: {}]",
                        affinityId,
                        previousCache.getClass().getName(),
                        cache.getClass().getName()
                );
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    static synchronized Cache getCacheInstance(final long affinityId) {
        READ_LOCK.lock();
        try {
            return CACHES.get(affinityId);
        } finally {
            READ_LOCK.unlock();
        }
    }

}
