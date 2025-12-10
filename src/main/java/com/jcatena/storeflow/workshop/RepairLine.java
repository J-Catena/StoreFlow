package com.jcatena.storeflow.workshop;

import com.jcatena.storeflow.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RepairOrder order;

    @ManyToOne
    private Product product;

    private int quantityUsed;
}
