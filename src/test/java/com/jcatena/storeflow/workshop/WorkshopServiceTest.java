package com.jcatena.storeflow.workshop;

import com.jcatena.storeflow.inventory.InventoryService;
import com.jcatena.storeflow.inventory.LocationType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkshopServiceTest {

    @Autowired
    private WorkshopService workshopService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void createOrder_createsOpenOrderWithNoLines() {
        var order = workshopService.createOrder("Cliente Taller", "Cortacésped Honda");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(RepairStatus.OPEN);
        assertThat(order.getLines()).isEmpty();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getClosedAt()).isNull();
    }

    @Test
    void addPart_addsNewLineAndAccumulatesQuantity() {
        inventoryService.addStock("PZ123", "Polea", 20);
        inventoryService.transferToWorkshop("PZ123", 10);

        var order = workshopService.createOrder("Cliente Taller", "Desbrozadora");

        workshopService.addPart(order.getId(), "PZ123", 3);
        var updated = workshopService.addPart(order.getId(), "PZ123", 2);

        assertThat(updated.getLines()).hasSize(1);
        var line = updated.getLines().get(0);
        assertThat(line.getQuantityUsed()).isEqualTo(5);
        assertThat(line.getProduct().getSerialNumber()).isEqualTo("PZ123");
    }

    @Test
    void closeOrder_deductsStockFromWorkshopOnly() {
        inventoryService.addStock("PZ123", "Polea", 20);
        inventoryService.transferToWorkshop("PZ123", 10);

        var order = workshopService.createOrder("Cliente Taller", "Motosierra");
        workshopService.addPart(order.getId(), "PZ123", 4);

        workshopService.closeOrder(order.getId());

        int storeStock = inventoryService.getStockForProductAt("PZ123", LocationType.STORE);
        int workshopStock = inventoryService.getStockForProductAt("PZ123", LocationType.WORKSHOP);

        assertThat(storeStock).isEqualTo(10); // 20 - 10 transfer
        assertThat(workshopStock).isEqualTo(6); // 10 - 4 used
    }

    @Test
    void closeOrder_failsWhenNotEnoughWorkshopStock() {
        inventoryService.addStock("PZ123", "Polea", 5);
        inventoryService.transferToWorkshop("PZ123", 5);

        var order = workshopService.createOrder("Cliente Taller", "Sopladora");
        workshopService.addPart(order.getId(), "PZ123", 10);

        assertThatThrownBy(() ->
                workshopService.closeOrder(order.getId())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough workshop stock");
    }

    @Test
    void closeOrder_failsIfAlreadyClosed() {
        inventoryService.addStock("PZ123", "Polea", 10);
        inventoryService.transferToWorkshop("PZ123", 10);

        var order = workshopService.createOrder("Cliente Taller", "Cortasetos");
        workshopService.addPart(order.getId(), "PZ123", 3);

        workshopService.closeOrder(order.getId());

        assertThatThrownBy(() ->
                workshopService.closeOrder(order.getId())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order already closed");
    }
}
