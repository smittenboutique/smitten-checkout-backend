package com.smitten.checkout;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SquareCheckoutService {

    private String squareBaseUrl() {
        String env = System.getenv("SQUARE_ENV");

        if ("production".equalsIgnoreCase(env)) {
            return "https://connect.squareup.com";
        }

        return "https://connect.squareupsandbox.com";
    }

    public void syncCatalog(SquareCatalogCache cache) {
    Map<String, Object> catalog = getCatalog();

    if (!Boolean.TRUE.equals(catalog.get("success"))) {
        throw new RuntimeException("Catalog sync failed: " + catalog.get("error"));
    }

    List<Map<String, Object>> objects =
            (List<Map<String, Object>>) catalog.get("objects");

    cache.clear();

    if (objects == null) {
        return;
    }

    for (Map<String, Object> obj : objects) {
        String type = (String) obj.get("type");

        if (!"ITEM_VARIATION".equals(type)) {
            continue;
        }

        Map<String, Object> variationData =
                (Map<String, Object>) obj.get("item_variation_data");

        if (variationData == null) {
            continue;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("id", obj.get("id"));
        product.put("catalog_object_id", obj.get("id"));
        product.put("name", variationData.getOrDefault("name", "Smitten Item"));

        Map<String, Object> priceMoney =
                (Map<String, Object>) variationData.get("price_money");

        if (priceMoney != null) {
            product.put("price", priceMoney.get("amount"));
            product.put("currency", priceMoney.get("currency"));
        }

        cache.put((String) obj.get("id"), product);
    }
}
