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
}
