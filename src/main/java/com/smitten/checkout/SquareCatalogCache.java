package com.smitten.checkout;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SquareCatalogCache {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public void put(String id, Object product)
    public Map<String, Object> getAll()

    public void clear() {
        cache.clear();
    }
}
