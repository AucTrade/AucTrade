package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.entity.ProductFile;
import com.example.auctrade.domain.product.repository.ProductFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService{
    private final ProductFileRepository productFileRepository;
    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    public Boolean uploadFile(MultipartFile[] uploadFiles, Long productId) throws IOException {
        if (uploadFiles == null) return false;

        for (MultipartFile file : uploadFiles) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath + fileName);
            Files.write(path, file.getBytes());
            productFileRepository.save(new ProductFile("/img/" + fileName, productId));
        }
        return true;
    }

    public List<String> getFiles(Long productId){
        return productFileRepository.findByProductId(productId).stream().map(ProductFile::getFilePath).toList();
    }

    public ProductFile getThumbnail(Long productId){
        return productFileRepository.findFirstByProductId(productId).orElse(null);
    }
}
