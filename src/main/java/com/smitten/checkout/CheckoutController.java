package com.smitten.checkout;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CheckoutController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ok");
        return res;
    }

    @GetMapping("/api/product")
    public Map<String, Object> getProduct(@RequestParam String id) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", "Product " + id);
        product.put("image", "https://via.placeholder.com/100");
        return product;
    }
}
