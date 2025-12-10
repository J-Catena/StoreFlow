package com.jcatena.storeflow;

import com.jcatena.storeflow.inventory.InventoryService;
import com.jcatena.storeflow.inventory.LocationType;
import com.jcatena.storeflow.sale.SaleService;
import com.jcatena.storeflow.workshop.WorkshopService;
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
    private final WorkshopService workshopService;

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

        System.out.println("---- TEST WORKSHOP ----");

// 1. Creamos stock en TIENDA
        inventoryService.addStock("PZ123", "Polea de motor", 20);

// 2. Transferimos 10 unidades de TIENDA -> TALLER
        inventoryService.transferToWorkshop("PZ123", 10);

// 3. Creamos orden de reparación
        var order = workshopService.createOrder("Cliente Taller", "Cortacésped Honda");

// 4. Añadimos pieza usada (5 uds)
        workshopService.addPart(order.getId(), "PZ123", 5);

// 5. Cerramos la reparación (ahora SÍ debe funcionar)
        var closed = workshopService.closeOrder(order.getId());

// 6. Comprobamos stock en taller y en tienda
        int storeStock = inventoryService.getStockForProductAt("PZ123", LocationType.STORE);
        int workshopStock = inventoryService.getStockForProductAt("PZ123", LocationType.WORKSHOP);

        System.out.println("Order status = " + closed.getStatus());
        System.out.println("Store stock after repair = " + storeStock);
        System.out.println("Workshop stock after repair = " + workshopStock);
        System.out.println("-------------------------");
    }
}

