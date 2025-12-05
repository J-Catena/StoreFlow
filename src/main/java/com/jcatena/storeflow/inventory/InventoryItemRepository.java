package com.jcatena.storeflow.inventory;

import com.jcatena.storeflow.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductAndLocation(Product product, LocationType location);
}
