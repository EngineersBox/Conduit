package com.engineersbox.conduit.core.schema.json.path;

import com.engineersbox.conduit.core.retrieval.caching.LRUCache;
import com.jayway.jsonpath.spi.cache.Cache;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class AffinityCacheProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityCacheProvider.class);
    private static final ConcurrentMutableMap<Long, Cache> CACHES = ConcurrentHashMap.newMap();
    private static final int DEFAULT_CACHE_CAPACITY = 10;
    private static final AtomicReference<Cache> DEFAULT_CACHE = new AtomicReference<>(new LRUCache(DEFAULT_CACHE_CAPACITY));

    private AffinityCacheProvider() {
        throw new UnsupportedOperationException("Static provider class");
    }

    public static void bindDefaultCache(final Cache cache) {
        DEFAULT_CACHE.set(cache);
    }

    public static void removeDefaultCache() {
        DEFAULT_CACHE.set(null);
    }

    static Cache getDefaultCache() {
        return DEFAULT_CACHE.get();
    }

    public static void bindCache(final long affinityId, final Cache cache) {
        final Cache previousCache = CACHES.put(affinityId, cache);
        if (previousCache != null) {
            LOGGER.warn(
                    "Overwritten previous JsonPath cache registration [Affinity ID: {}] [Previous: {}] [New: {}]",
                    affinityId,
                    previousCache.getClass().getName(),
                    cache.getClass().getName()
            );
        }
    }

    static Cache getCacheInstance(final long affinityId) {
        return CACHES.getOrDefault(
                affinityId,
                DEFAULT_CACHE.get()
        );
    }

}
