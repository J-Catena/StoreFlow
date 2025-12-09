package com.jcatena.storeflow.sale;

import com.jcatena.storeflow.inventory.InventoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class SaleServiceTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void createTicket_createsOpenTicketWithNoLines() {
        var ticket = saleService.createTicket("Cliente Test");

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getLines()).isEmpty();
        assertThat(ticket.getCreatedAt()).isNotNull();
        assertThat(ticket.getClosedAt()).isNull();
    }

    @Test
    void addLine_addsNewLineWhenProductNotInTicket() {
        inventoryService.addStock("ABC123", "Producto Test", 10);

        var ticket = saleService.createTicket("Cliente Test");

        var updated = saleService.addLine(ticket.getId(), "ABC123", 3);

        assertThat(updated.getLines()).hasSize(1);

        var line = updated.getLines().get(0);
        assertThat(line.getQuantity()).isEqualTo(3);
        assertThat(line.getProduct().getSerialNumber()).isEqualTo("ABC123");
    }

    @Test
    void addLine_sumsQuantityWhenProductAlreadyAdded() {
        inventoryService.addStock("ABC123", "Producto Test", 10);

        var ticket = saleService.createTicket("Cliente Test");

        saleService.addLine(ticket.getId(), "ABC123", 2);
        var updated = saleService.addLine(ticket.getId(), "ABC123", 5);

        assertThat(updated.getLines()).hasSize(1);
        assertThat(updated.getLines().get(0).getQuantity()).isEqualTo(7);
    }

    @Test
    void addLine_shouldFailIfTicketClosed() {
        inventoryService.addStock("ABC123", "Producto Test", 10);

        var ticket = saleService.createTicket("Cliente Test");
        saleService.closeTicket(ticket.getId());

        assertThatThrownBy(() ->
                saleService.addLine(ticket.getId(), "ABC123", 3)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void closeTicket_shouldDeductStock() {
        inventoryService.addStock("ABC123", "Producto Test", 10);

        var ticket = saleService.createTicket("Cliente Test");
        saleService.addLine(ticket.getId(), "ABC123", 4);

        saleService.closeTicket(ticket.getId());

        int stock = inventoryService.getStockForProduct("ABC123");
        assertThat(stock).isEqualTo(6);
    }

    @Test
    void closeTicket_shouldFailWhenNotEnoughStock() {
        inventoryService.addStock("ABC123", "Producto Test", 5);

        var ticket = saleService.createTicket("Cliente Test");
        saleService.addLine(ticket.getId(), "ABC123", 10);

        assertThatThrownBy(() ->
                saleService.closeTicket(ticket.getId())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void closeTicket_shouldFailIfAlreadyClosed() {
        inventoryService.addStock("ABC123", "Producto Test", 5);

        var ticket = saleService.createTicket("Cliente Test");
        saleService.addLine(ticket.getId(), "ABC123", 3);

        saleService.closeTicket(ticket.getId()); // close first time

        assertThatThrownBy(() ->
                saleService.closeTicket(ticket.getId()) // second time
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }
}
