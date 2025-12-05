package com.jcatena.storeflow.sale;

import com.jcatena.storeflow.inventory.InventoryItem;
import com.jcatena.storeflow.inventory.InventoryItemRepository;
import com.jcatena.storeflow.inventory.LocationType;
import com.jcatena.storeflow.product.Product;
import com.jcatena.storeflow.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleTicketRepository saleTicketRepository;
    private final SaleLineRepository saleLineRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    // -------------------------
    // 1. Crear ticket
    // -------------------------
    @Transactional
    public SaleTicket createTicket(String customerName) {
        SaleTicket ticket = SaleTicket.builder()
                .createdAt(LocalDateTime.now())
                .status(TicketStatus.OPEN)
                .customerName(customerName)
                .build();
        return saleTicketRepository.save(ticket);
    }

    // -------------------------
    // 2. Añadir líneas
    // -------------------------
    @Transactional
    public SaleTicket addLine(Long ticketId, String serialNumber, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        SaleTicket ticket = saleTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Cannot add lines to a closed or cancelled ticket");
        }

        Product product = productRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + serialNumber));

        // ¿Existe ya una línea con este producto?
        SaleLine existing = ticket.getLines().stream()
                .filter(line -> line.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            SaleLine newLine = SaleLine.builder()
                    .ticket(ticket)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                    .build();
            ticket.getLines().add(newLine);
        }

        return saleTicketRepository.save(ticket);
    }


    // -------------------------
    // 3. Cerrar ticket (descargar stock)
    // -------------------------
    @Transactional
    public SaleTicket closeTicket(Long ticketId) {

        SaleTicket ticket = saleTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new IllegalStateException("Ticket already closed or cancelled");
        }

        // 1. Validar stock línea por línea
        for (SaleLine line : ticket.getLines()) {
            Product product = line.getProduct();

            InventoryItem item = inventoryItemRepository
                    .findByProductAndLocation(product, LocationType.STORE)
                    .orElseThrow(() -> new IllegalStateException("No inventory entry for product: " + product.getSerialNumber()));

            if (item.getQuantity() < line.getQuantity()) {
                throw new IllegalStateException(
                        "Not enough stock for product " + product.getSerialNumber() +
                                ". Requested " + line.getQuantity() +
                                ", available " + item.getQuantity()
                );
            }
        }

        // 2. Si todo tiene stock suficiente → descontar
        for (SaleLine line : ticket.getLines()) {
            InventoryItem item = inventoryItemRepository
                    .findByProductAndLocation(line.getProduct(), LocationType.STORE)
                    .orElseThrow();

            item.setQuantity(item.getQuantity() - line.getQuantity());
            inventoryItemRepository.save(item);
        }

        // 3. Cambiar estado del ticket
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        return saleTicketRepository.save(ticket);
    }

    public SaleTicket getTicket(Long ticketId) {
        return saleTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }
}
