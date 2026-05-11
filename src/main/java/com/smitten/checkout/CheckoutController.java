package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CheckoutController {

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    @GetMapping("/fbcheckout")
    public String fbcheckout() {
        return "fbcheckout";
    }

    @PostMapping("/api/create-checkout")
@ResponseBody
public Map<String, Object> createCheckout(@RequestBody Map<String, Object> body) {

    Map<String, Object> result = new HashMap<>();

    try {
        String token = System.getenv("SQUARE_ACCESS_TOKEN");
        String locationId = System.getenv("SQUARE_LOCATION_ID");

        if (token == null || locationId == null) {
            throw new RuntimeException("Missing Square environment variables");
        }

        List<Map<String, Object>> items =
                (List<Map<String, Object>>) body.get("items");

        List<Map<String, Object>> lineItems = new java.util.ArrayList<>();

        if (items != null) {
            for (Map<String, Object> item : items) {

                int qty = Integer.parseInt(String.valueOf(item.get("qty")));

                Map<String, Object> lineItem = new HashMap<>();
                lineItem.put("name", "Smitten Item " + item.get("id"));
                lineItem.put("quantity", String.valueOf(qty));

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

        Map<String, Object> checkoutRequest = new HashMap<>();
        checkoutRequest.put("order", order);
        checkoutRequest.put("idempotency_key", java.util.UUID.randomUUID().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(checkoutRequest, headers);

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://connect.squareupsandbox.com/v2/online-checkout/payment-links";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();
        Map paymentLink = (Map) responseBody.get("payment_link");

        result.put("success", true);
        result.put("checkout_url", paymentLink.get("url"));

    } catch (Exception e) {
        result.put("success", false);
        result.put("error", e.getMessage());
    }

    return result;
}
}
