package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import com.squareup.square.SquareClient;
import com.squareup.square.Environment;

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
        product.put("id", id);
        product.put("name", "Product " + id);
        product.put("image", "https://via.placeholder.com/100");
        return product;
    }

    @GetMapping("/api/square-test")
@ResponseBody
public Map<String, Object> squareTest() {

    Map<String, Object> result = new HashMap<>();

    try {

        String token = System.getenv("SQUARE_ACCESS_TOKEN");

        SquareClient client = new SquareClient.Builder()
        .environment(Environment.SANDBOX)
        .accessToken(token)
        .build();

        result.put("success", true);
        result.put("message", "Square client initialized");

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
