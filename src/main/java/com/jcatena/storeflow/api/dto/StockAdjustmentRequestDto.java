package com.jcatena.storeflow.api.dto;

import com.jcatena.storeflow.inventory.LocationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;



public record StockAdjustmentRequestDto(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "location is required")
        LocationType location,

        @NotNull(message = "delta is required")
        Integer delta,

        @Size(max = 200, message = "reason must be at most 200 characters")
        String reason
) {
}
