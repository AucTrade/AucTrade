package com.example.auctrade.domain.product.entity;

import com.example.auctrade.domain.product.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @JoinColumn(name = "sale_username", nullable = false)
    private String saleUsername;

    public Product(ProductDTO.Create requestDto, ProductCategory category) {
        this.name = requestDto.getName();
        this.detail = requestDto.getDetail();
        this.category = category;
        this.saleUsername = requestDto.getSaleUsername();
    }
}
