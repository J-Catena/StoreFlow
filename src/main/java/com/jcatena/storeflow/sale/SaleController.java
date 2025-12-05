package com.jcatena.storeflow.sale;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping("/tickets")
    public ResponseEntity<SaleTicketResponse> createTicket(
            @RequestBody CreateTicketRequest request
    ) {
        var ticket = saleService.createTicket(request.getCustomerName());
        return ResponseEntity.ok(SaleTicketResponse.fromEntity(ticket));
    }

    @PostMapping("/tickets/{ticketId}/lines")
    public ResponseEntity<SaleTicketResponse> addLine(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddLineRequest request
    ) {
        var updated = saleService.addLine(ticketId, request.getSerialNumber(), request.getQuantity());
        return ResponseEntity.ok(SaleTicketResponse.fromEntity(updated));
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<SaleTicketResponse> closeTicket(@PathVariable Long ticketId) {
        var closed = saleService.closeTicket(ticketId);
        return ResponseEntity.ok(SaleTicketResponse.fromEntity(closed));
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<SaleTicketResponse> getTicket(@PathVariable Long ticketId) {
        var ticket = saleService.getTicket(ticketId);
        return ResponseEntity.ok(SaleTicketResponse.fromEntity(ticket));
    }
}
