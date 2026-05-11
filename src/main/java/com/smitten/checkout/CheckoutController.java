package com.smitten.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public Map<String, Object> createCheckout() {

        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("checkout_url", "https://squareup.com/checkout/test");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
