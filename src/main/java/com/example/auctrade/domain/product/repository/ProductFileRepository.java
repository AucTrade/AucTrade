package com.example.auctrade.domain.product.repository;

import com.example.auctrade.domain.product.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFileRepository extends JpaRepository<ProductFile, Long> {
    List<ProductFile> findByProductId(Long productId);
}
