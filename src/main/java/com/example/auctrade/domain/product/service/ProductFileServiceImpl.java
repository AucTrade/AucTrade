package com.example.auctrade.domain.product.service;

import com.example.auctrade.domain.product.entity.ProductFile;
import com.example.auctrade.domain.product.repository.ProductFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service
@Transactional
@Slf4j(topic = "File Service")
public class ProductFileServiceImpl implements ProductFileService {
    private final ProductFileRepository productFileRepository;
    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    public ProductFileServiceImpl(ProductFileRepository productFileRepository){
        this.productFileRepository = productFileRepository;
    }

    /**
     * 파일 업로드
     * @param uploadFiles 업로드할 파일 정보
     * @param productId 상품 ID
     * @return 파일 업로드 성공 여부
     */
    @Override
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

    /**
     * 파일 로드
     * @param productId 상품 ID
     * @return 대상 파일 주소
     */
    @Override
    public List<String> getFiles(Long productId){
        return productFileRepository.findByProductId(productId).stream().map(ProductFile::getFilePath).toList();
    }
    
    /**
     * 파일 썸네일 조회
     * @param productId 상품 ID
     * @return 파일 썸네일
     */
    @Override
    public ProductFile getThumbnail(Long productId){
        return productFileRepository.findFirstByProductId(productId).orElse(null);
    }
}
