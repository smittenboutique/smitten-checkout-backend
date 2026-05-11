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
        List<Map<String, Object>> items =
                (List<Map<String, Object>>) body.get("items");

        // build Square line items
        List<Map<String, Object>> lineItems = new java.util.ArrayList<>();

        if (items != null) {
            for (Map<String, Object> item : items) {

                Map<String, Object> lineItem = new HashMap<>();

                lineItem.put("name", "Smitten Item " + item.get("id"));
                lineItem.put("quantity", String.valueOf(item.get("qty")));

                lineItem.put("base_price_money", Map.of(
                        "amount", 1000, // temporary fixed price ($10)
                        "currency", "USD"
                ));

                lineItems.add(lineItem);
            }
        }

        Map<String, Object> order = new HashMap<>();
        order.put("location_id", "8678GDF01W6SW");
        order.put("line_items", lineItems);

        Map<String, Object> request = new HashMap<>();
        request.put("order", order);
        request.put("idempotency_key", java.util.UUID.randomUUID().toString());

        result.put("success", true);
        result.put("checkout_url", "https://squareup.com/checkout/test");

    } catch (Exception e) {
        result.put("success", false);
        result.put("error", e.getMessage());
    }

    return result;
}
}
