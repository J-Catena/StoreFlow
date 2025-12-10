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
     * Stock en TIENDA (por compatibilidad con lo que ya usas).
     */
    @Transactional
    public int getStockForProduct(String serialNumber) {
        return getStockForProductAt(serialNumber, LocationType.STORE);
    }

    /**
     * Stock por ubicación (STORE, WORKSHOP...).
     */
    @Transactional
    public int getStockForProductAt(String serialNumber, LocationType location) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return 0;
        }

        String cleanedSerial = serialNumber.trim();

        return productRepository.findBySerialNumber(cleanedSerial)
                .flatMap(product ->
                        inventoryItemRepository.findByProductAndLocation(product, location)
                )
                .map(InventoryItem::getQuantity)
                .orElse(0);
    }

    /**
     * Transfiere stock de TIENDA a TALLER.
     */
    @Transactional
    public void transferToWorkshop(String serialNumber, int quantity) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number must not be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        String cleanedSerial = serialNumber.trim();

        Product product = productRepository.findBySerialNumber(cleanedSerial)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + cleanedSerial));

        // Stock en tienda
        InventoryItem storeItem = inventoryItemRepository
                .findByProductAndLocation(product, LocationType.STORE)
                .orElseThrow(() -> new IllegalStateException(
                        "No store stock entry for product: " + cleanedSerial
                ));

        if (storeItem.getQuantity() < quantity) {
            throw new IllegalStateException(
                    "Not enough store stock for product " + cleanedSerial +
                            ". Requested transfer " + quantity +
                            ", available " + storeItem.getQuantity()
            );
        }

        // Stock en taller (crear si no existe)
        InventoryItem workshopItem = inventoryItemRepository
                .findByProductAndLocation(product, LocationType.WORKSHOP)
                .orElseGet(() -> InventoryItem.builder()
                        .product(product)
                        .location(LocationType.WORKSHOP)
                        .quantity(0)
                        .build()
                );

        // Movimiento
        storeItem.setQuantity(storeItem.getQuantity() - quantity);
        workshopItem.setQuantity(workshopItem.getQuantity() + quantity);

        inventoryItemRepository.save(storeItem);
        inventoryItemRepository.save(workshopItem);
    }
}
