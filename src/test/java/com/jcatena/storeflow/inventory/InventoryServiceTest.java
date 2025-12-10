package com.jcatena.storeflow.inventory;

import com.jcatena.storeflow.product.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Test
    void addStock_createsProductIfNotExists_andAccumulatesQuantity() {
        // 1. Añadimos 10 unidades (crea el producto)
        inventoryService.addStock("PZ123", "Polea motor", 10);

        // 2. Añadimos otras 5 unidades del mismo serial
        inventoryService.addStock("PZ123", "Polea motor", 5);

        int storeStock = inventoryService.getStockForProductAt("PZ123", LocationType.STORE);

        assertThat(storeStock).isEqualTo(15);

        // Validamos que solo existe un producto
        assertThat(productRepository.findAll()).hasSize(1);
    }

    @Test
    void getStockForProductAt_returnsZeroIfProductNotExists() {
        int stock = inventoryService.getStockForProductAt("NO_EXISTE", LocationType.STORE);
        assertThat(stock).isEqualTo(0);
    }

    @Test
    void transferToWorkshop_movesStockFromStoreToWorkshop() {
        // Tenemos 20 unidades en tienda
        inventoryService.addStock("PZ999", "Correa", 20);

        // Transferimos 8 al taller
        inventoryService.transferToWorkshop("PZ999", 8);

        int storeStock = inventoryService.getStockForProductAt("PZ999", LocationType.STORE);
        int workshopStock = inventoryService.getStockForProductAt("PZ999", LocationType.WORKSHOP);

        assertThat(storeStock).isEqualTo(12);
        assertThat(workshopStock).isEqualTo(8);
    }

    @Test
    void transferToWorkshop_failsWhenNotEnoughStoreStock() {
        inventoryService.addStock("PZ777", "Tornillo", 5);

        assertThatThrownBy(() ->
                inventoryService.transferToWorkshop("PZ777", 10)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough store stock");
    }

    @Test
    void transferToWorkshop_createsWorkshopEntryIfNotExists() {
        inventoryService.addStock("ZZ001", "Rodamiento", 12);

        // Transferimos 4 al taller
        inventoryService.transferToWorkshop("ZZ001", 4);

        var itemWorkshop = inventoryItemRepository
                .findByProductAndLocation(
                        productRepository.findBySerialNumber("ZZ001").orElseThrow(),
                        LocationType.WORKSHOP
                );

        assertThat(itemWorkshop).isPresent();
        assertThat(itemWorkshop.get().getQuantity()).isEqualTo(4);
    }
}
