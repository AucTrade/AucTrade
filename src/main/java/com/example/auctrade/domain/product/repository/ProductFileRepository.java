package com.example.auctrade.domain.product.repository;

import com.example.auctrade.domain.product.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductFileRepository extends JpaRepository<ProductFile, Long> {
    List<ProductFile> findByProductId(Long productId);
    Optional<ProductFile> findFirstByProductId(Long productId);

}
