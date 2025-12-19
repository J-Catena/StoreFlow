package com.jcatena.storeflow.api.dto;

import com.jcatena.storeflow.inventory.LocationType;


public record StockAdjustmentResponseDto(
        Long productId,
        LocationType location,
        int delta,
        int newQuantity
) {
}
