package com.smitten.checkout;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

private final SquareCheckoutService squareService;

public CheckoutController(SquareCheckoutService squareService) {
    this.squareService = squareService;
}

@GetMapping("/health")
public Map<String, Object> health() {
    return Map.of("status", "ok");
}

@GetMapping("/catalog")
public Map<String, Object> getCatalog() {
    return squareService.getCatalog();
}

@PostMapping("/create-checkout")
public Map<String, Object> createCheckout(
        @RequestBody Map<String, Object> body) {

    return squareService.createCheckout(body);
}

}
