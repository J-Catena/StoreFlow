package com.jcatena.storeflow.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddStockRequest {

    @NotBlank
    private String serialNumber;

    private String name;

    @Min(1)
    private int quantity;
}
