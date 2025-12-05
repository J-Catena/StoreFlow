package com.jcatena.storeflow.sale;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddLineRequest {

    @NotBlank
    private String serialNumber;

    @Min(1)
    private int quantity;
}
