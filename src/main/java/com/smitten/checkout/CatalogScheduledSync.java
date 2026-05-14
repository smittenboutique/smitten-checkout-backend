package com.smitten.checkout;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogScheduledSync {

    private final SquareCheckoutService squareService;
    private final SquareCatalogCache cache;

    public CatalogScheduledSync(
            SquareCheckoutService squareService,
            SquareCatalogCache cache
    ) {
        this.squareService = squareService;
        this.cache = cache;
    }

    @Scheduled(fixedRate = 300000)
    public void refreshCatalog() {
        squareService.syncCatalog(cache);
        System.out.println("Catalog refreshed on schedule. Items cached: " + cache.getAll().size());
    }
}
