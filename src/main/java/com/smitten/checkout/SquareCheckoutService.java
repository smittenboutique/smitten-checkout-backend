package com.smitten.checkout;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SquareCheckoutService {

    public void syncCatalog(SquareCatalogCache cache) {

        try {
            String token = System.getenv("SQUARE_ACCESS_TOKEN");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            String url = "https://connect.squareupsandbox.com/v2/catalog/list";

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            List<Map<String, Object>> objects =
                    (List<Map<String, Object>>) response.getBody().get("objects");

            cache.clear();

            for (Map<String, Object> obj : objects) {

                String type = (String) obj.get("type");

                if (!"ITEM_VARIATION".equals(type)) continue;

                Map varData = (Map) obj.get("item_variation_data");
                if (varData == null) continue;

                Map<String, Object> product = new HashMap<>();

                product.put("id", obj.get("id"));
                product.put("name", varData.get("name"));

                Map priceMoney = (Map) varData.get("price_money");
                if (priceMoney != null) {
                    product.put("price", priceMoney.get("amount"));
                }

                cache.put((String) obj.get("id"), product);
            }

        } catch (Exception e) {
            throw new RuntimeException("Catalog sync failed: " + e.getMessage());
        }
    }
}
