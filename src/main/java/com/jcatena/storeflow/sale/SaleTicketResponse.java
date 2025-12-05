package com.jcatena.storeflow.sale;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleTicketResponse {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private TicketStatus status;
    private String customerName;
    private List<SaleLineResponse> lines;

    @Data
    @Builder
    public static class SaleLineResponse {
        private Long id;
        private String serialNumber;
        private String productName;
        private int quantity;
    }

    public static SaleTicketResponse fromEntity(SaleTicket ticket) {
        return SaleTicketResponse.builder()
                .id(ticket.getId())
                .createdAt(ticket.getCreatedAt())
                .closedAt(ticket.getClosedAt())
                .status(ticket.getStatus())
                .customerName(ticket.getCustomerName())
                .lines(
                        ticket.getLines().stream()
                                .map(line -> SaleLineResponse.builder()
                                        .id(line.getId())
                                        .serialNumber(line.getProduct().getSerialNumber())
                                        .productName(line.getProduct().getName())
                                        .quantity(line.getQuantity())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}
