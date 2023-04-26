package com.engineersbox.conduit.schema.provider;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.cache.Cache;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache implements Cache {

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private final int size;
    private final LinkedHashMap<String, JsonPath> entries;

    public LRUCache(final int size) {
        this.size = size;
        int capacity = (int) Math.ceil(size / DEFAULT_LOAD_FACTOR) + 1;
        this.entries = new LinkedHashMap<>(capacity, DEFAULT_LOAD_FACTOR, true) {
            // (an anonymous inner class)
            @Serial
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, JsonPath> eldest) {
                return size() > LRUCache.this.size;
            }
        };
    }

    public void put(final String key,
                    final JsonPath value) {
        synchronized (this) {
            this.entries.put(key, value);
        }
    }

    public JsonPath get(final String key) {
        synchronized (this) {
            return this.entries.get(key);
        }
    }

    public int size() {
        synchronized (this) {
            return this.entries.size();
        }
    }

    public String toString() {
        synchronized (this) {
            return this.entries.toString();
        }
    }
}
