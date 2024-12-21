package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.bid.entity.BidLog;
import com.example.auctrade.domain.bid.repository.BidLogRepository;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidStatus;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.product.entity.Product;
import com.example.auctrade.domain.product.entity.ProductCategory;
import com.example.auctrade.domain.product.repository.ProductCategoryRepository;
import com.example.auctrade.domain.product.repository.ProductRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import com.example.auctrade.global.exception.CustomException;
import com.example.auctrade.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@Transactional
class BidLogServiceImplTest {
    @Autowired
    BidLogService bidLogService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    BidLogRepository bidLogRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryRepository productCategoryRepository;
    MultipartFile[] mockFiles;
    ProductCategory productCategory;
    Product product;
    @BeforeEach
    void createData(){
        mockFiles = new MultipartFile[]{
                new MockMultipartFile("file", "testfile.png",
                "image/png", "Hello World".getBytes())};

        productCategory = productCategoryRepository.save(ProductCategory.builder().categoryName("잡화").build());
        product = productRepository.save(Product.builder()
                .name("제품명")
                .detail("제품 상세 설명")
                .category(productCategory)
                .userId(1L)
                .build());
    }

    @AfterEach
    void deleteEachData(){
        userRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        bidLogRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("경매 입찰 로그 등록시 해당 ID 반환 및 status CREATE 데이터 저장")
    void createBidLog() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 4000;
        BidVo bidVo = generateBid(auction, user, AMOUNT);

        //When
        Long result = bidLogService.createBidLog(bidVo, BidStatus.CREATE);

        //Then
        BidLog bidLog = bidLogRepository.findById(result).orElse(null);
        assertNotNull(bidLog, "데이터가 DB에 저장 되지 않았습니다.");
        assertEquals(bidLog.getId() , result, "반환된 ID 가 일치하지 않습니다.");
        assertEquals(BidStatus.CREATE, bidLog.getStatus(), "저장된 상태가 CREATE 가 아닙니다.");
    }

    @Test
    @DisplayName("잘못된 경매 ID로 경매 입찰 로그 상태 업데이트 요청 시 BID_LOG_NOT_FOUND 메시지 발생")
    void updateLogStatusInvalidAuctionId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> bidLogService.updateLogStatus(-1L, user.getId(), BidStatus.CANCEL));

        //Then
        assertEquals(ErrorCode.BID_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }
    @Test
    @DisplayName("잘못된 유저 ID로 경매 입찰 로그 상태 업데이트 요청 시 BID_LOG_NOT_FOUND 메시지 발생")
    void updateLogStatusInvalidUserId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> bidLogService.updateLogStatus(auction.getId(), -1L, BidStatus.CANCEL));

        //Then
        assertEquals(ErrorCode.BID_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("정상 입찰 로그 상태 업데이트 요청 시 해당 ID 반환")
    void updateLogStatus() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        Long result = bidLogService.updateLogStatus(auction.getId(), user.getId(), BidStatus.CANCEL);

        //Then
        BidLog bidLog = bidLogRepository.findById(result).orElse(null);
        assertEquals(BidStatus.CANCEL, bidLog.getStatus());
    }

    @Test
    @DisplayName("잘못된 경매 ID로 입찰 로그 조회 요청 시 BID_LOG_NOT_FOUND 메시지 발생")
    void getBidLogInvalidAuctionId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> bidLogService.getBidLog(-1L, user.getId()));

        //Then
        assertEquals(ErrorCode.BID_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("잘못된 유저 ID로 입찰 로그 조회 요청 시 BID_LOG_NOT_FOUND 메시지 발생")
    void getBidLogInvalidUserId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> bidLogService.getBidLog(auction.getId(), -1L));

        //Then
        assertEquals(ErrorCode.BID_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }


    @Test
    @DisplayName("정상 입찰 로그 조회 요청 시 해당 로그 정보 반환")
    void getBidLog() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 5000;
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        BidInfoVo result = bidLogService.getBidLog(auction.getId(), user.getId());

        //Then
        Assertions.assertAll(
                () -> assertEquals(auction.getId(), result.getAuctionId(), "조회된 경매의 ID가 예상값과 다릅니다."),
                () -> assertEquals(user.getId(), result.getUserId(), "조회된 유저의 ID가 예상값과 다릅니다."),
                () -> assertEquals(AMOUNT, result.getAmount(), "조회된 입찰금이 예상값과 다릅니다."),
                () -> assertEquals( BidStatus.CREATE, result.getStatus(), "조회된 상태가 예상값과 다릅니다.")
        );
    }

    @Test
    @DisplayName("존재하지 않는 로그 데이터 조회 요청 시 false 반환")
    void containsUserIdNotExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        Boolean result = bidLogService.containsUserId(auction.getId(), -1L);

        //Then
        assertFalse(result);
    }

    @Test
    @DisplayName("존재하는 로그 데이터 조회 요청 시 true 반환")
    void containsUserIdExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        bidLogService.createBidLog(generateBid(auction, user, 5000), BidStatus.CREATE);

        //When
        Boolean result = bidLogService.containsUserId(auction.getId(), user.getId());

        //Then
        assertTrue(result);
    }

    private BidVo generateBid(Auction auction, User user, Integer amount){
        return BidVo.builder()
                .auctionId(auction.getId())
                .userId(user.getId())
                .amount(amount)
                .build();
    }

    private Auction generateAuction(Long userId){
        return generateAuction(userId,30, 3000);
    }

    private User generateUser() {
        return User.builder()
                .email(UUID.randomUUID()+"@test.com")
                .password("123")
                .phone("010-0000-0000")
                .postcode("12341")
                .birth(LocalDate.now())
                .address("address")
                .role(UserRoleEnum.USER)
                .build();
    }

    private Auction generateAuction(Long userId, Integer maxParticipants, Integer minimumPrice){
        LocalDateTime now = LocalDateTime.now();
        return Auction.builder()
                .userId(userId)
                .title("제목")
                .introduce("내용")
                .maxParticipants(maxParticipants)
                .productId(product.getId())
                .minimumPrice(minimumPrice)
                .startAt(now.plusDays(1))
                .endAt(now.plusDays(2))
                .isEnded(false)
                .build();
    }
}