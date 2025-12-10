package com.jcatena.storeflow.workshop;

import com.jcatena.storeflow.product.Product;
import jakarta.persistence.*;
import lombok.*;
import com.jcatena.storeflow.workshop.RepairStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String machineDescription;

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    private RepairStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RepairLine> lines = new ArrayList<>();
}