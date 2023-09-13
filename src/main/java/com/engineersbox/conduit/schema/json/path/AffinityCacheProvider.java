package com.engineersbox.conduit.schema.json.path;

import com.jayway.jsonpath.spi.cache.Cache;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class AffinityCacheProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityCacheProvider.class);
    private static final ConcurrentMutableMap<Long, Cache> CACHES = ConcurrentHashMap.newMap();

    private AffinityCacheProvider() {
        throw new UnsupportedOperationException("Static provider class");
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

    static synchronized Cache getCacheInstance(final long affinityId) {
        return CACHES.get(affinityId);
    }

}