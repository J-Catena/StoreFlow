package com.jcatena.storeflow.api.dto;

import java.math.BigDecimal;

public record InventorySummaryDto(
        Long productId,
        String sku,
        String name,
        BigDecimal storeQty,
        BigDecimal workshopQty,
        BigDecimal totalQty
) {
}
