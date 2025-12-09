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

    @Test
    void addStock_createsNewProductAndInventoryItem() {
        inventoryService.addStock("ABC123", "Producto Test", 10);

        var product = productRepository.findBySerialNumber("ABC123");
        assertThat(product).isPresent();

        int stock = inventoryService.getStockForProduct("ABC123");
        assertThat(stock).isEqualTo(10);
    }

    @Test
    void addStock_addsQuantityWhenProductExists() {
        inventoryService.addStock("ABC123", "Producto Test", 5);
        inventoryService.addStock("ABC123", "Producto Test", 7);

        int stock = inventoryService.getStockForProduct("ABC123");
        assertThat(stock).isEqualTo(12);
    }

    @Test
    void getStock_returnsZeroWhenProductDoesNotExist() {
        int stock = inventoryService.getStockForProduct("NO_EXISTE");
        assertThat(stock).isEqualTo(0);
    }
}
