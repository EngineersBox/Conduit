package com.engineersbox.conduit.core.schema.json.path;

import com.engineersbox.conduit.core.jvm.AgentInspector;
import com.engineersbox.conduit.core.retrieval.caching.LRUCache;
import com.jayway.jsonpath.spi.cache.Cache;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@ThreadSafe
public class AffinityCacheProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityCacheProvider.class);
    private static final ConcurrentMutableMap<Long, Cache> CACHES = ConcurrentHashMap.newMap();
    private static final int DEFAULT_CACHE_CAPACITY = 10;
    private static final AtomicReference<Cache> DEFAULT_CACHE = new AtomicReference<>(new LRUCache(DEFAULT_CACHE_CAPACITY));
    private static final boolean _ENABLED = AgentInspector.agentLoaded(Pattern.compile(".*aspectjweaver.*"));
    static {
        if (_ENABLED) {
            LOGGER.info("AspectJ runtime weaver detected, AffinityCacheProvider will intercept calls to JsonPath CacheProvider");
        } else {
            LOGGER.warn("AspectJ runtime weaver agent is not loaded, AffinityCacheProvider will be transparent to JsonPath CacheProvider");
        }
    }

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
        if (!_ENABLED) {
            return;
        }
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
        if (!_ENABLED) {
            return null;
        }
        return CACHES.getOrDefault(
                affinityId,
                DEFAULT_CACHE.get()
        );
    }

}
