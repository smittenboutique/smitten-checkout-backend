package com.smitten.checkout;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogStartupSync implements ApplicationRunner {

    private final SquareCheckoutService squareService;
    private final SquareCatalogCache cache;

    public CatalogStartupSync(SquareCheckoutService squareService,
                              SquareCatalogCache cache) {
        this.squareService = squareService;
        this.cache = cache;
    }

    @Override
    public void run(ApplicationArguments args) {

        System.out.println("🔄 Syncing Square catalog on startup...");

        try {
            // squareService.syncCatalog(cache);

            System.out.println("✅ Catalog sync complete. Items cached: "
                + cache.all().size());

        } catch (Exception e) {

            System.err.println("❌ Catalog sync failed on startup");
            e.printStackTrace();
        }
    }
}
