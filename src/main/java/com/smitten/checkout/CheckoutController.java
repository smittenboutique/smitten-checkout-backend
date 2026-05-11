package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class CheckoutController {

    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    @GetMapping("/api/product")
@ResponseBody
public Map<String, Object> getProduct(@RequestParam String id) {

    Map<String, Object> product = new HashMap<>();

    try {

        String token = System.getenv("SQUARE_ACCESS_TOKEN");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        String url =
            "https://connect.squareupsandbox.com/v2/catalog/object/" + id;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map obj = (Map) response.getBody().get("object");

        Map itemData = (Map) obj.get("item_data");

        product.put("id", obj.get("id"));
        product.put("name", itemData.get("name"));

    } catch (Exception e) {

        product.put("error", e.getMessage());
    }

    return product;
}

  @GetMapping("/api/square-test")
@ResponseBody
public Map<String, Object> squareTest() {

    Map<String, Object> result = new HashMap<>();

    try {

        String token = System.getenv("SQUARE_ACCESS_TOKEN");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://connect.squareupsandbox.com/v2/catalog/list";

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        result.put("success", true);
        result.put("status", response.getStatusCode().toString());

    } catch (Exception e) {

        result.put("success", false);
        result.put("error", e.getMessage());
    }

    return result;
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

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // SIMPLE HARD-CODED TEST ORDER FIRST
        Map<String, Object> order = new HashMap<>();
        order.put("location_id", "8678GDF01W6SW");

        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("name", "Smitten Product");
        lineItem.put("quantity", "1");
        lineItem.put("base_price_money", Map.of(
                "amount", 1000,
                "currency", "USD"
        ));

        order.put("line_items", List.of(lineItem));

        Map<String, Object> request = new HashMap<>();
        request.put("order", order);
        request.put("idempotency_key", java.util.UUID.randomUUID().toString());
        request.put("ask_for_shipping_address", false);

        Map<String, Object> checkoutRequest = new HashMap<>();
        checkoutRequest.put("order", order);
        checkoutRequest.put("idempotency_key", java.util.UUID.randomUUID().toString());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(checkoutRequest, headers);

        String url = "https://connect.squareupsandbox.com/v2/online-checkout/payment-links";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                entity,
                Map.class
        );

        Map responseBody = response.getBody();

        Map checkout = (Map) responseBody.get("payment_link");

        result.put("success", true);
        result.put("checkout_url", checkout.get("url"));

    } catch (Exception e) {
        result.put("success", false);
        result.put("error", e.getMessage());
    }

    return result;
}
}
