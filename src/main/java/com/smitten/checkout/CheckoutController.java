package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class CheckoutController {

    // -----------------------
    // HEALTH CHECK
    // -----------------------
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }

    // -----------------------
    // FRONTEND PAGE
    // -----------------------
    @GetMapping("/fbcheckout")
    public String fbcheckout() {
        return "fbcheckout";
    }

    // -----------------------
    // PRODUCT (TEMP MOCK SAFE)
    // Replace later with Square Catalog API
    // -----------------------
    @GetMapping("/api/product")
    @ResponseBody
    public Map<String, Object> getProduct(@RequestParam String id) {

        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", "Smitten Product " + id);
        product.put("image", "https://via.placeholder.com/50");

        return product;
    }

    // -----------------------
    // CREATE CHECKOUT (SQUARE PAYMENT LINKS)
    // -----------------------
    @PostMapping("/api/create-checkout")
    @ResponseBody
    public Map<String, Object> createCheckout(@RequestBody Map<String, Object> body) {

        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("STEP 1: method entered");
            
            String token = System.getenv("SQUARE_ACCESS_TOKEN");
            String locationId = System.getenv("SQUARE_LOCATION_ID");
            
            System.out.println("STEP 2: env vars loaded");
            System.out.println("TOKEN EXISTS: " + (token != null));
            System.out.println("LOCATION EXISTS: " + (locationId != null));

            if (token == null || locationId == null) {
                throw new RuntimeException("Missing Square environment variables");
            }

            // Build simple line item (safe baseline)
            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) body.get("items");

            List<Map<String, Object>> lineItems = new ArrayList<>();

            if (items == null || items.isEmpty()) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("name", "Smitten Test Item");
                fallback.put("quantity", "1");
                fallback.put("base_price_money", Map.of(
                    "amount", 1000,
                    "currency", "USD"
            ));
            lineItems.add(fallback);
}

            if (items != null) {
                for (Map<String, Object> item : items) {

                    Map<String, Object> lineItem = new HashMap<>();
                    lineItem.put("name", "Smitten Item " + item.get("id"));
                    lineItem.put("quantity", String.valueOf(item.get("qty")));

                    lineItem.put("base_price_money", Map.of(
                            "amount", 1000,
                            "currency", "USD"
                    ));

                    lineItems.add(lineItem);
                }
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

            System.out.println("STEP 3: sending Square request");
            
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            Map responseBody = response.getBody();

            System.out.println("STEP 4: Square responded");
            System.out.println(responseBody);

            if (responseBody == null || responseBody.get("payment_link") == null) {
                throw new RuntimeException("Invalid Square response: " + responseBody);
            }

            Map paymentLink = (Map) responseBody.get("payment_link");

            result.put("success", true);
            result.put("checkout_url", paymentLink.get("url"));

        } catch (Exception e) {
            result.put("success", false);
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        return result;
    }
}
