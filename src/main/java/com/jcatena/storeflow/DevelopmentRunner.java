package com.jcatena.storeflow;

import com.jcatena.storeflow.inventory.InventoryService;
import com.jcatena.storeflow.sale.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // este runner solo se ejecutará si activas el perfil "dev"
@RequiredArgsConstructor
public class DevelopmentRunner implements CommandLineRunner {

    private final InventoryService inventoryService;
    private final SaleService saleService;

    @Override
    public void run(String... args) {

        System.out.println("---- TEST SALE ----");

        // 1. Añadimos stock
        inventoryService.addStock("ABC123", "Cuchilla cortacésped 40cm", 10);

        // 2. Creamos ticket
        var ticket = saleService.createTicket("Cliente de prueba");

        // 3. Añadimos líneas
        saleService.addLine(ticket.getId(), "ABC123", 3);
        saleService.addLine(ticket.getId(), "ABC123", 2);

        // 4. Cerramos ticket
        saleService.closeTicket(ticket.getId());

        // 5. Comprobamos stock final esperado: 10 - (3+2) = 5
        int remaining = inventoryService.getStockForProduct("ABC123");

        System.out.println("Stock después de la venta = " + remaining);
        System.out.println("-------------------------");
    }
}
