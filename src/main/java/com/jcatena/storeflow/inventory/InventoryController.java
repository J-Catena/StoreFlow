package com.jcatena.storeflow.inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add")
    public ResponseEntity<Integer> addStock(@Valid @RequestBody AddStockRequest request) {
        var item = inventoryService.addStock(
                request.getSerialNumber(),
                request.getName(),
                request.getQuantity()
        );
        return ResponseEntity.ok(item.getQuantity());
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<Integer> getStock(@PathVariable String serialNumber) {
        int stock = inventoryService.getStockForProduct(serialNumber);
        return ResponseEntity.ok(stock);
    }
}
