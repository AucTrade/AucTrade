package com.example.auctrade.domain.limit.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.service.FileService;
import com.example.auctrade.domain.product.service.ProductService;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LimitServiceImpl implements LimitService{
	private final UserService userService;
	private final ProductService productService;
	private final LimitRepository limitRepository;
	private final FileService fileService;

	@Override
	public LimitDTO.Get save(LimitDTO.Create limitDTO, MultipartFile[] imgFiles, String email) throws IOException {
		// 상품 생성
		long productId = productService.create(ProductDTO.Create.builder()
			.saleUsername(email)
			.productCategoryId(limitDTO.getProductCategoryId())
			.name(limitDTO.getProductName())
			.detail(limitDTO.getProductDetail())
			.build());

		// 이미지 파일 업로드
		if(Boolean.FALSE.equals(fileService.uploadFile(imgFiles, productId)))
			throw new CustomException(ErrorCode.WRONG_MULTIPARTFILE);

		// 한정 판매 등록
		Limits limits = LimitMapper.toEntity(limitDTO, productId, email); // userEmail 사용
		limitRepository.save(limits);

		// Product 정보 가져오기
		ProductDTO.Get product = productService.get(productId);
		return LimitMapper.toDto(limits, product, email);
	}

	@Override
	public List<LimitDTO.Get> findAll() {
		return limitRepository.findAll().stream()
			.map(limit -> LimitMapper.toDto(limit, getProduct(limit.getProductId()), limit.getSeller()))
			.toList();
	}

	@Override
	public LimitDTO.Get findById(Long id) {
		Limits limit = limitRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
		return LimitMapper.toDto(limit, getProduct(limit.getProductId()), limit.getSeller());
	}

	@Override
	public void endLimit(Long id) {
		Limits limit = limitRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.LIMIT_NOT_FOUND));
		limit.end();
		limitRepository.save(limit);
	}

	@Override
	public List<LimitDTO.Get> findByUserEmail(String email) {
		List<Limits> limits = limitRepository.findAllBySeller(email);
		return limits.stream()
			.map(limit -> LimitMapper.toDto(limit, getProduct(limit.getProductId()), email))
			.toList();
	}

	public ProductDTO.Get getProduct(Long productId) {
		return productService.get(productId);
	}

	@Override
	public LimitDTO.GetPage getMyLimitedPage(int page, int size, String status, String email){
		if(status.equals("all")) return getAllMyLimits(page, size, email);

		return getAllMyLimits(page, size, email);

	}

	private LimitDTO.GetPage getAllMyLimits(int page, int size, String email) {
		Page<Limits> limits = limitRepository.findBySaleUserId(userService.getUserIdByEmail(email), toPageable(page, size, "saleDate"));

		List<LimitDTO.Get> limitDTOList = limits.getContent().stream()
			.map(limit -> LimitMapper.toDto(limit, getProduct(limit.getProductId()), email))
			.toList();

		return new LimitDTO.GetPage(limitDTOList, (long) limits.getTotalPages());
	}
	private Pageable toPageable(int page, int size, String target){
		return PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, target));
	}
}
