package com.jcatena.storeflow.sale;

import com.jcatena.storeflow.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ticket al que pertenece esta línea
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ticket_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_saleline_ticket")
    )
    private SaleTicket ticket;

    // Producto vendido
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_saleline_product")
    )
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
}
