package com.smitten.checkout;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SquareCheckoutService {

    // -----------------------
    // SYNC CATALOG
    // -----------------------
    public void syncCatalog(SquareCatalogCache cache) {

        try {
            String token = System.getenv("SQUARE_ACCESS_TOKEN");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            String url =
                "https://connect.squareupsandbox.com/v2/catalog/list";

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

    // parent ITEM reference (this is key fix)
    List<String> itemIds = (List<String>) varData.get("item_id");
    String itemId = itemIds != null && !itemIds.isEmpty()
            ? itemIds.get(0)
            : null;

    Map<String, Object> product = new HashMap<>();

    // variation ID = checkout ID (critical)
    product.put("variation_id", obj.get("id"));

    // fallback name safety
    product.put("name",
        varData.getOrDefault("name", "Smitten Item")
    );

    // price safety
    Map priceMoney = (Map) varData.get("price_money");
    if (priceMoney != null) {
        product.put("price", priceMoney.get("amount"));
    } else {
        product.put("price", 1000);
    }

    product.put("item_id", itemId);

    cache.put((String) obj.get("id"), product);
}
                
            }

        } catch (Exception e) {
            throw new RuntimeException("Catalog sync failed: " + e.getMessage());
        }
    }

    // -----------------------
    // GET RAW CATALOG
    // -----------------------
    public Map<String, Object> getCatalog() {

        Map<String, Object> result = new HashMap<>();

        try {
            String token = System.getenv("SQUARE_ACCESS_TOKEN");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            String url =
                "https://connect.squareupsandbox.com/v2/catalog/list";

            ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            result.put("success", true);
            result.put("objects", response.getBody().get("objects"));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // -----------------------
    // CREATE CHECKOUT
    // -----------------------
    public Map<String, Object> createCheckout(Map<String, Object> body) {

        Map<String, Object> result = new HashMap<>();

        try {
            String token = System.getenv("SQUARE_ACCESS_TOKEN");
            String locationId = System.getenv("SQUARE_LOCATION_ID");

            if (token == null || token.isBlank()) {
                throw new RuntimeException("Missing Square Access Token");
            }

            if (locationId == null || locationId.isBlank()) {
                throw new RuntimeException("Missing Square Location ID");
            }

            List<Map<String, Object>> items =
                (List<Map<String, Object>>) body.get("items");

            if (items == null || items.isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            List<Map<String, Object>> lineItems = new ArrayList<>();

            for (Map<String, Object> item : items) {

                if (item.get("catalog_object_id") == null) {
                    throw new RuntimeException(
                        "Missing catalog_object_id in cart item"
                    );
                }

                Map<String, Object> lineItem = new HashMap<>();

                lineItem.put(
                    "catalog_object_id",
                    item.get("catalog_object_id")
                );

                lineItem.put(
                    "quantity",
                    String.valueOf(item.get("quantity"))
                );

                lineItems.add(lineItem);
            }

            Map<String, Object> order = new HashMap<>();
            order.put("location_id", locationId);
            order.put("line_items", lineItems);

            Map<String, Object> request = new HashMap<>();
            request.put("order", order);
            request.put("idempotency_key", UUID.randomUUID().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, headers);

            RestTemplate restTemplate = new RestTemplate();

            String url =
                "https://connect.squareupsandbox.com/v2/online-checkout/payment-links";

            ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

            Map bodyResponse = response.getBody();

            if (bodyResponse == null ||
                bodyResponse.get("payment_link") == null) {
                throw new RuntimeException("Invalid Square response: " + bodyResponse);
            }

            Map paymentLink =
                (Map) bodyResponse.get("payment_link");

            result.put("success", true);
            result.put("checkout_url", paymentLink.get("url"));

        } catch (Exception e) {

            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
