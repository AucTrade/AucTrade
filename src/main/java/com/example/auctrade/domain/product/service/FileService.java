package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.entity.ProductFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    Boolean uploadFile(MultipartFile[] uploadFiles, Long productId) throws IOException;

    List<String> getFiles(Long productId);

    ProductFile getThumbnail(Long productId);
}
