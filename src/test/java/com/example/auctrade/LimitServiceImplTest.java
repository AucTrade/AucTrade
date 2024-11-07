package com.example.auctrade;

import com.example.auctrade.domain.limit.dto.LimitDTO;
import com.example.auctrade.domain.limit.entity.Limits;
import com.example.auctrade.domain.limit.mapper.LimitMapper;
import com.example.auctrade.domain.limit.repository.LimitRepository;
import com.example.auctrade.domain.limit.service.LimitServiceImpl;
import com.example.auctrade.domain.product.dto.ProductDTO;
import com.example.auctrade.domain.product.service.FileService;
import com.example.auctrade.domain.product.service.ProductService;
import com.example.auctrade.domain.user.service.UserService;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimitServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private ProductService productService;

	@Mock
	private LimitRepository limitRepository;

	@Mock
	private FileService fileService;

	@InjectMocks
	private LimitServiceImpl limitService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Save new limit - successful")
	void saveLimit_success() throws IOException {
		LimitDTO.Create createDto = LimitDTO.Create.builder()
			.title("New Limit")
			.introduce("Limit Intro")
			.price(100L)
			.saleDate(null)
			.amount(10)
			.personalLimit(1)
			.productName("Product")
			.productCategoryId(1L)
			.seller("seller@test.com")
			.build();

		MultipartFile[] files = {};
		String email = "seller@test.com";

		when(productService.create(any())).thenReturn(1L);
		when(fileService.uploadFile(files, 1L)).thenReturn(true);
		when(limitRepository.save(any())).thenReturn(Limits.builder().id(1L).build());

		LimitDTO.Get result = limitService.save(createDto, files, email);

		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	@DisplayName("Save new limit - file upload failure")
	void saveLimit_fileUploadFailure() {
		LimitDTO.Create createDto = LimitDTO.Create.builder().title("New Limit").build();
		MultipartFile[] files = {};
		String email = "seller@test.com";

		when(productService.create(any())).thenReturn(1L);
		when(fileService.uploadFile(files, 1L)).thenReturn(false);

		CustomException exception = assertThrows(CustomException.class, () -> {
			limitService.save(createDto, files, email);
		});

		assertEquals(ErrorCode.WRONG_MULTIPARTFILE, exception.getErrorCode());
	}

	@Test
	@DisplayName("Find all limits")
	void findAllLimits() {
		Limits limit = Limits.builder().id(1L).title("Limit").build();
		when(limitRepository.findAll()).thenReturn(List.of(limit));
		when(productService.get(limit.getProductId())).thenReturn(new ProductDTO.Get());

		List<LimitDTO.Get> limits = limitService.findAll();

		assertNotNull(limits);
		assertEquals(1, limits.size());
	}

	@Test
	@DisplayName("Find limit by ID - success")
	void findLimitById_success() {
		Limits limit = Limits.builder().id(1L).title("Limit").build();
		when(limitRepository.findById(1L)).thenReturn(Optional.of(limit));
		when(productService.get(limit.getProductId())).thenReturn(new ProductDTO.Get());

		LimitDTO.Get result = limitService.findById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	@DisplayName("Find limit by ID - not found")
	void findLimitById_notFound() {
		when(limitRepository.findById(1L)).thenReturn(Optional.empty());

		CustomException exception = assertThrows(CustomException.class, () -> {
			limitService.findById(1L);
		});

		assertEquals(ErrorCode.LIMIT_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("Get all limits for a user by email")
	void getLimitsByUserEmail() {
		Limits limit = Limits.builder().id(1L).title("Limit").build();
		when(limitRepository.findAllBySeller("user@test.com")).thenReturn(List.of(limit));
		when(productService.get(limit.getProductId())).thenReturn(new ProductDTO.Get());

		List<LimitDTO.Get> limits = limitService.findByUserEmail("user@test.com");

		assertNotNull(limits);
		assertEquals(1, limits.size());
	}

	@Test
	@DisplayName("Get paginated limits by status for user")
	void getMyLimitedPage() {
		Limits limit = Limits.builder().id(1L).title("Limit").build();
		Page<Limits> page = new PageImpl<>(List.of(limit));
		Pageable pageable = PageRequest.of(0, 10);

		when(userService.getUserIdByEmail("user@test.com")).thenReturn(1L);
		when(limitRepository.findBySaleUserId(1L, pageable)).thenReturn(page);
		when(productService.get(limit.getProductId())).thenReturn(new ProductDTO.Get());

		LimitDTO.GetPage result = limitService.getMyLimitedPage(1, 10, "all", "user@test.com");

		assertNotNull(result);
		assertEquals(1, result.getLimits().size());
	}
}
