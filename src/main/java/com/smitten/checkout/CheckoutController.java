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
public Map<String, Object> createCheckout(@RequestBody Map<String, Object> body) {

    Map<String, Object> result = new HashMap<>();

    List<Map<String, Object>> items = null;

try {
    items = (List<Map<String, Object>>) body.get("items");
} catch (Exception e) {
    result.put("success", false);
    result.put("error", "Invalid cart format");
    return result;
}
}
