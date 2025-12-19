package com.jcatena.storeflow.api.dto;

import com.jcatena.storeflow.inventory.LocationType;


public record InventoryItemDto(
        Long productId,
        String sku,
        String name,
        LocationType location,
        int quantity
) {
}
