package com.example.auctrade.domain.product.entity;

import com.example.auctrade.domain.product.dto.ProductDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "detail")
    private String detail;

    @ManyToOne
    @JoinColumn(name = "category", nullable = false)
    private ProductCategory category;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public Product(String name, String detail, Long userId, ProductCategory category) {
        this.name = name;
        this.detail = detail;
        this.category = category;
        this.userId = userId;
    }
}
