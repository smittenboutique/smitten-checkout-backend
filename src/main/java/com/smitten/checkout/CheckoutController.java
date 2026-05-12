@PostMapping("/api/create-checkout")
public Map<String, Object> createCheckout(@RequestBody Map<String, Object> body) {
    return squareService.createCheckout(body);
}
