package com.smitten.checkout;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SquareCatalogCache {

    private final Map<String, Map<String, Object>> products = new HashMap<>();

    public void put(String id, Map<String, Object> product) {
        products.put(id, product);
    }

    public Map<String, Object> get(String id) {
        return products.get(id);
    }

    public Collection<Map<String, Object>> all() {
        return products.values();
    }

    public void clear() {
        products.clear();
    }
}
