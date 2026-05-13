package com.smitten.checkout;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SquareCatalogCache {

    private final Map<String, Map<String, Object>> cache =
            new ConcurrentHashMap<>();

    public void put(String id, Map<String, Object> product) {
        cache.put(id, product);
    }

    public Map<String, Object> getAll() {
        return cache;
    }

    public void clear() {
        cache.clear();
    }
}
