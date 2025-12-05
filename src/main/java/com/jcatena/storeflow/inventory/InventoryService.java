package com.jcatena.storeflow.inventory;

import com.jcatena.storeflow.product.Product;
import com.jcatena.storeflow.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Añade stock a la TIENDA.
     * Si el producto no existe (por serialNumber), lo crea.
     */
    @Transactional
    public InventoryItem addStock(String serialNumber, String name, int quantity) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number must not be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        String cleanedSerial = serialNumber.trim();

        // 1. Buscar producto por serialNumber
        Product product = productRepository.findBySerialNumber(cleanedSerial)
                .orElseGet(() -> {
                    // Crear producto nuevo si no existe
                    Product newProduct = Product.builder()
                            .serialNumber(cleanedSerial)
                            .name(name != null && !name.isBlank() ? name.trim() : cleanedSerial)
                            .active(true)
                            .build();
                    return productRepository.save(newProduct);
                });

        // 2. Buscar registro de inventario en la TIENDA
        InventoryItem inventoryItem = inventoryItemRepository
                .findByProductAndLocation(product, LocationType.STORE)
                .orElseGet(() -> InventoryItem.builder()
                        .product(product)
                        .location(LocationType.STORE)
                        .quantity(0)
                        .build()
                );

        // 3. Sumar cantidad
        int newQuantity = inventoryItem.getQuantity() + quantity;
        inventoryItem.setQuantity(newQuantity);

        // 4. Guardar y devolver
        return inventoryItemRepository.save(inventoryItem);
    }

    /**
     * Devuelve el stock actual en TIENDA para el serialNumber dado.
     * Si no existe producto o inventario, devuelve 0.
     */
    @Transactional
    public int getStockForProduct(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return 0;
        }

        String cleanedSerial = serialNumber.trim();

        return productRepository.findBySerialNumber(cleanedSerial)
                .flatMap(product ->
                        inventoryItemRepository.findByProductAndLocation(product, LocationType.STORE)
                )
                .map(InventoryItem::getQuantity)
                .orElse(0);
    }
}
