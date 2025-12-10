package com.jcatena.storeflow.workshop;

import com.jcatena.storeflow.inventory.InventoryItem;
import com.jcatena.storeflow.inventory.InventoryItemRepository;
import com.jcatena.storeflow.inventory.LocationType;
import com.jcatena.storeflow.product.Product;
import com.jcatena.storeflow.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final RepairOrderRepository repairOrderRepository;
    private final RepairLineRepository repairLineRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public RepairOrder createOrder(String customerName, String machineDescription) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name required");
        }

        RepairOrder order = RepairOrder.builder()
                .customerName(customerName)
                .machineDescription(machineDescription)
                .createdAt(LocalDateTime.now())
                .status(RepairStatus.OPEN)
                .build();

        return repairOrderRepository.save(order);
    }

    @Transactional
    public RepairOrder addPart(Long orderId, String serialNumber, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != RepairStatus.OPEN) {
            throw new IllegalStateException("Cannot modify closed order");
        }

        Product product = productRepository.findBySerialNumber(serialNumber.trim())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // ¿Ya hay línea con este producto?
        RepairLine existing = order.getLines().stream()
                .filter(l -> l.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantityUsed(existing.getQuantityUsed() + quantity);
        } else {
            RepairLine newLine = RepairLine.builder()
                    .order(order)
                    .product(product)
                    .quantityUsed(quantity)
                    .build();
            order.getLines().add(newLine);
        }

        return repairOrderRepository.save(order);
    }

    @Transactional
    public RepairOrder closeOrder(Long orderId) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != RepairStatus.OPEN) {
            throw new IllegalStateException("Order already closed");
        }

        // 1. Validar stock en WORKSHOP
        for (RepairLine line : order.getLines()) {
            Product product = line.getProduct();

            InventoryItem workshopItem = inventoryItemRepository
                    .findByProductAndLocation(product, LocationType.WORKSHOP)
                    .orElseThrow(() -> new IllegalStateException(
                            "No workshop stock for product: " + product.getSerialNumber()
                    ));

            if (workshopItem.getQuantity() < line.getQuantityUsed()) {
                throw new IllegalStateException(
                        "Not enough workshop stock for product: " + product.getSerialNumber()
                );
            }
        }

        // 2. Descontar stock en WORKSHOP
        for (RepairLine line : order.getLines()) {
            InventoryItem workshopItem = inventoryItemRepository
                    .findByProductAndLocation(line.getProduct(), LocationType.WORKSHOP)
                    .orElseThrow(); // ya lo hemos validado antes

            workshopItem.setQuantity(workshopItem.getQuantity() - line.getQuantityUsed());
            inventoryItemRepository.save(workshopItem);
        }

        // 3. Cerrar orden
        order.setStatus(RepairStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());

        return repairOrderRepository.save(order);
    }
}
