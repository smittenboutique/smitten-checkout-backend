package com.smitten.checkout;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class CheckoutController {

    private final SquareCheckoutService squareService;

    public CheckoutController(SquareCheckoutService squareService) {
        this.squareService = squareService;
    }

    @GetMapping("/api/health")
        public Map<String, Object> health(SquareCatalogCache cache) {
        return Map.of(
            "status", "ok",
            "catalog_loaded", cache.all().size()
        );
    }

    @GetMapping("/fbcheckout")
        public String fbcheckout() {
        return "redirect:/fbcheckout.html";
    }

    @PostMapping("/api/create-checkout")
        public Map<String, Object> createCheckout(@RequestBody Map<String, Object> body) {
        return squareService.createCheckout(body);
    }

    @GetMapping("/api/catalog")
        public Map<String, Object> getCatalog() {
        return squareService.getCatalog();
    }
    
    @GetMapping("/api/synced-catalog")
        public Map<String, Object> getSyncedCatalog(SquareCatalogCache cache) {
        return Map.of(
            "success", true,
            "products", cache.all()
        );
    }
}
