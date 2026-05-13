package com.smitten.checkout;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogStartupSync implements CommandLineRunner {

    private final SquareCheckoutService squareService;
    private final SquareCatalogCache cache;

    public CatalogStartupSync(
            SquareCheckoutService squareService,
            SquareCatalogCache cache
    ) {
        this.squareService = squareService;
        this.cache = cache;
    }

    @Override
    public void run(String... args) {
        squareService.syncCatalog(cache);
        System.out.println("Catalog synced on startup");
    }
}
