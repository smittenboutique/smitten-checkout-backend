package com.smitten.checkout;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SquareCheckoutService {
private Map<String, Object> discountForCoupon(String coupon) {

    if (coupon == null || coupon.isBlank()) {
        return null;
    }

    String code = coupon.trim().toUpperCase();

    switch (code) {

        case "WELCOME":
            return Map.of(
                    "uid", "WELCOME",
                    "name", "WELCOME 10% OFF",
                    "type", "FIXED_PERCENTAGE",
                    "percentage", "10",
                    "scope", "ORDER"
            );

        default:
            return null;
    }
}
    private String squareBaseUrl() {
        String env = System.getenv("SQUARE_ENV");

        if ("production".equalsIgnoreCase(env)) {
            return "https://connect.squareup.com";
        }

        return "https://connect.squareupsandbox.com";
    }

    public void syncCatalog(SquareCatalogCache cache) {
        Map<String, Object> catalog = getCatalog();
Map<String, String> imageMap = new HashMap<>();
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
if ("IMAGE".equals(type)) {

    Map<String, Object> imageData =
            (Map<String, Object>) obj.get("image_data");

    if (imageData != null) {

        String imageUrl =
                (String) imageData.get("url");

        imageMap.put(
                (String) obj.get("id"),
                imageUrl
        );
    }

    continue;
}
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
            String imageId =
        (String) variationData.get("image_id");

if (imageId != null) {
    product.put("image_url", imageMap.get(imageId));
}

            Map<String, Object> priceMoney =
                    (Map<String, Object>) variationData.get("price_money");

            if (priceMoney != null) {
                product.put("price", priceMoney.get("amount"));
                product.put("currency", priceMoney.get("currency"));
            }

            cache.put((String) obj.get("id"), product);
        }
    }

    public Map<String, Object> getCatalog() {
        Map<String, Object> result = new HashMap<>();

        try {
            String token = System.getenv("SQUARE_ACCESS_TOKEN");

            if (token == null || token.isBlank()) {
                throw new RuntimeException("Missing Square Access Token");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            String url = squareBaseUrl() + "/v2/catalog/list?types=ITEM,ITEM_VARIATION,IMAGE";

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();

            result.put("success", true);
            result.put("objects", body == null ? null : body.get("objects"));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

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
                    throw new RuntimeException("Missing catalog_object_id in cart item");
                }

                Map<String, Object> lineItem = new HashMap<>();
                lineItem.put("catalog_object_id", item.get("catalog_object_id"));
                lineItem.put("quantity", String.valueOf(item.get("quantity")));

                lineItems.add(lineItem);
            }

            Map<String, Object> order = new HashMap<>();
            order.put("location_id", locationId);
            order.put("line_items", lineItems);
            
String coupon = (String) body.get("coupon");
boolean freeShipping =
        "VIPSHIP".equalsIgnoreCase(coupon);
            
if ("WELCOME".equalsIgnoreCase(coupon)) {
    order.put("discounts", List.of(
        Map.of(
            "uid", "WELCOME",
            "name", "WELCOME 10% OFF",
            "type", "FIXED_PERCENTAGE",
            "percentage", "10",
            "scope", "ORDER"
        )
    ));
}
            if (freeShipping) {

    order.put("metadata", Map.of(
            "free_shipping_coupon", "VIPSHIP"
    ));
}
            
            Map<String, Object> request = new HashMap<>();
            String source = (String) body.get("source");
String campaign = (String) body.get("campaign");
            request.put("order", order);
            if (source != null || campaign != null) {

    Map<String, String> metadata = new HashMap<>();

    if (source != null) {
        metadata.put("source", source);
    }

    if (campaign != null) {
        metadata.put("campaign", campaign);
    }

    order.put("metadata", metadata);
}
            request.put("idempotency_key", UUID.randomUUID().toString());
            String appBaseUrl = System.getenv("APP_BASE_URL");

if (appBaseUrl == null || appBaseUrl.isBlank()) {
    appBaseUrl = "https://smitten-checkout-backend.onrender.com";
}

request.put("checkout_options", Map.of(
        "redirect_url",
        appBaseUrl + "/success.html"
));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            RestTemplate restTemplate = new RestTemplate();

            String url = squareBaseUrl() + "/v2/online-checkout/payment-links";

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || responseBody.get("payment_link") == null) {
                throw new RuntimeException("Invalid Square response: " + responseBody);
            }

            Map<String, Object> paymentLink =
                    (Map<String, Object>) responseBody.get("payment_link");

            result.put("success", true);
            result.put("checkout_url", paymentLink.get("url"));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
