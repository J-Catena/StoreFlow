package com.jcatena.storeflow.mapper;

import com.jcatena.storeflow.api.dto.InventoryItemDto;
import com.jcatena.storeflow.api.dto.InventorySummaryDto;
import com.jcatena.storeflow.inventory.InventoryItem;
import com.jcatena.storeflow.inventory.LocationType;

import java.math.BigDecimal;

public class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryItemDto toItemDto(InventoryItem item) {
        return new InventoryItemDto(
                item.getProduct().getId(),
                item.getProduct().getSerialNumber(),
                item.getProduct().getName(),
                item.getLocation(),
                item.getQuantity()
        );
    }

    public static InventorySummaryDto toSummaryDto(
            Long productId,
            String sku,
            String name,
            BigDecimal storeQty,
            BigDecimal workshopQty
    ) {
        BigDecimal total = storeQty.add(workshopQty);
        return new InventorySummaryDto(productId, sku, name, storeQty, workshopQty, total);
    }

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
