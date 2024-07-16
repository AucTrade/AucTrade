package com.example.auctrade.domain.product.entity;

import com.example.auctrade.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 상품 등록자
}