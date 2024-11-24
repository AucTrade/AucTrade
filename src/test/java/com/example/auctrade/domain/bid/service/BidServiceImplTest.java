package com.example.auctrade.domain.bid.service;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.bid.vo.BidInfoVo;
import com.example.auctrade.domain.bid.vo.BidVo;
import com.example.auctrade.domain.deposit.service.DepositService;
import com.example.auctrade.domain.deposit.vo.DepositVo;
import com.example.auctrade.domain.point.repository.PointRepository;
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
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class BidServiceImplTest {
    @Autowired
    BidService bidService;
    @Autowired
    DepositService depositService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductCategoryRepository productCategoryRepository;
    @Autowired
    RedissonClient redissonClient;

    MultipartFile[] mockFiles;

    ProductCategory productCategory;
    Product product;
    List<User> multiUsers;
    Auction multiAuction;
    @BeforeAll
    void createMultiThreadData() {

        mockFiles = new MultipartFile[]{new MockMultipartFile("file", "testfile.png",
                "image/png", "Hello World".getBytes())};

        productCategory = productCategoryRepository.save(ProductCategory.builder().categoryName("잡화").build());
        product = productRepository.save(Product.builder()
                .name("제품명")
                .detail("제품 상세 설명")
                .category(productCategory)
                .userId(1L)
                .build());

        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);
        multiAuction = auctionRepository.save(generateAuction(user.getId(),30,3000));

        multiUsers = new ArrayList<>();

        for(int i = 0 ; i < 10 ; i++){
            User theOther = generateUser();
            theOther.addPoint(50000);
            multiUsers.add(userRepository.save(theOther));
        }
    }

    @AfterEach
    void deleteEachData(){
        userRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("경매 입찰시 입찰금이 부족한 경우 WRONG_DEPOSIT_CREATE 에러 메시지 발생")
    void placeBidInvalidDepositAmount() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> bidService.placeBid(generateBid(auction, user, 4000)));

        //Than
        assertEquals(ErrorCode.WRONG_BID_AMOUNT.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("경매 입찰시 이전 입찰가 이하인 경우 false 반환")
    void placeBidInvalidBidAmount() {
        //Given
        User anotherUser = userRepository.save(generateUser());
        anotherUser.addPoint(50000);
        anotherUser = userRepository.save(anotherUser);

        Auction auction = auctionRepository.save(generateAuction(anotherUser.getId()));

        depositService.placeDeposit(generateDeposit(auction, anotherUser, 5000));
        bidService.placeBid(generateBid(auction, anotherUser, 4000));

        User user = userRepository.save(generateUser());
        user.addPoint(50000);
        user = userRepository.save(user);
        depositService.placeDeposit(generateDeposit(auction, user, 5000));

        //When
        Boolean result = bidService.placeBid(generateBid(auction, user, 4000));

        //Then
        assertFalse(result, "이전 입찰가 이하의 입찰이 등록되었습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("경매 정상 입찰시 ture 반환 및 데이터 저장")
    void placeBid() {
        //Given
        User user = userRepository.save(generateUser());
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositService.placeDeposit(generateDeposit(auction, user, 5000));

        //When
        Boolean result =  bidService.placeBid(generateBid(auction, user, 4000));

        //Then
        assertTrue(result, "정상 입찰 요청이 실패 하였습니다.");

        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auction.getId());
        assertEquals(user.getEmail(),bidMap.get(BID_USER_KEY), "입찰한 유저의 이메일이 일치하지 않습니다.");
        assertEquals(4000, Integer.parseInt(bidMap.get(BID_PRICE_KEY)), "입찰금이 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("경매 입찰시 이전 입찰가 보다 높은 금액일 시 true 반환 및 데이터 저장")
    void placeBidRequestMoreThanBefore() {
        //Given
        User anotherUser = userRepository.save(generateUser());
        anotherUser.addPoint(50000);
        anotherUser = userRepository.save(anotherUser);

        Auction auction = auctionRepository.save(generateAuction(anotherUser.getId()));

        depositService.placeDeposit(generateDeposit(auction, anotherUser, 5000));
        bidService.placeBid(generateBid(auction, anotherUser, 4000));

        User user = userRepository.save(generateUser());
        user.addPoint(50000);
        user = userRepository.save(user);
        final int AMOUNT = 6000;
        depositService.placeDeposit(generateDeposit(auction, user, AMOUNT));

        //When
        Boolean result = bidService.placeBid(generateBid(auction, user, AMOUNT));

        //Then
        assertTrue(result, "정상 입찰 요청이 실패 하였습니다.");

        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auction.getId());
        assertEquals(user.getEmail(), bidMap.get(BID_USER_KEY), "입찰한 유저의 이메일이 일치하지 않습니다.");
        assertEquals(AMOUNT, Integer.parseInt(bidMap.get(BID_PRICE_KEY)), "입찰금이 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("입찰이 없는 경매의 입찰 정보 조회시 email = NONE, amont = -1 반환")
    void getBidInfoWithoutBid() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));

        //When
        BidInfoVo result = bidService.getBidInfo(auction.getId());

        //Then
        assertEquals(auction.getId(), result.getAuctionId(), "경매 ID가 일치하지 않습니다.");
        assertEquals("NONE", result.getEmail(), "이메일이 NONE이 아닙니다.");
        assertEquals(-1, result.getAmount(), "조회된 금액이 -1이 아닙니다.");
    }

    @Test
    @DisplayName("정상 적인 입찰 정보 조회시 해당 정보 반환")
    void getBidInfo() {
        //Given
        User user = userRepository.save(generateUser());
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositService.placeDeposit(generateDeposit(auction, user, 6000));
        bidService.placeBid(generateBid(auction, user, 6000));

        //When
        BidInfoVo result = bidService.getBidInfo(auction.getId());

        //Then
        assertEquals(auction.getId(), result.getAuctionId(), "경매 ID가 일치하지 않습니다.");
        assertEquals(user.getEmail(), result.getEmail(), "이메일이 일치하지 않습니다.");
        assertEquals(6000, result.getAmount(), "조회된 금액이 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("입찰이 없는 경매의 입찰 취소 요청시 REDIS_INTERNAL_ERROR 메시지 발생")
    void cancelBidWithoutBid() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));

        //When
        CustomException result = assertThrows(CustomException.class,
                () -> bidService.cancelBid(auction.getId(), user.getEmail()),
                "입찰이 없는 경매의 입찰이 취소 되었습니다."
        );

        //Then
        assertEquals(ErrorCode.REDIS_INTERNAL_ERROR.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("입찰 되지 않은 유저의 입찰 취소 요청시 REDIS_INTERNAL_ERROR 메시지 발생")
    void cancelBidInvalidUserEmail() {
        //Given
        User user = userRepository.save(generateUser());
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositService.placeDeposit(generateDeposit(auction, user, 6000));

        //when
        CustomException result = assertThrows(CustomException.class,
                () -> bidService.cancelBid(auction.getId(), "email"),
                "입찰되지 않은 유저의 취소 요청이 적용 되었습니다."
        );

        //Then
        assertEquals(ErrorCode.REDIS_INTERNAL_ERROR.getMessage(), result.getMessage());

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("정상 입찰 취소 요청시 true 반환 및 해당 데이터 삭제")
    void cancelBid() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositService.placeDeposit(generateDeposit(auction, user, 6000));

        //When
        Boolean result = depositService.cancelDeposit(auction.getId(), user.getId());

        //Then
        assertTrue(result, "정상 입찰 취소 요청에 실패하였습니다.");

        RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auction.getId());
        assertNull(bidMap.get(BID_USER_KEY), "해당 취소 요청이 적용되지 않았습니다.");
        assertNull(bidMap.get(BID_PRICE_KEY), "해당 취소 요청이 적용되지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + auction.getId());
    }

    @Test
    @DisplayName("같은 가격 동시 입찰 요청 ")
    void multiDepositRequest() throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);  // 동시 요청 개수 설정
        List<Boolean> results = new ArrayList<>();  // 성공 여부 확인 리스트

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + multiAuction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + multiAuction.getId());
        for (int i = 0; i < 10; i++) {
            int finalI = i;

            executor.submit(() -> {
                try {
                    depositService.placeDeposit((generateDeposit(multiAuction, multiUsers.get(finalI),4000)));
                    boolean success = bidService.placeBid(generateBid(multiAuction, multiUsers.get(finalI),4000));
                    synchronized (results) {
                        results.add(success);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        // 모든 스레드가 끝날 때까지 대기
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        int successfulDeposits = (int) results.stream().filter(Boolean::booleanValue).count();
        Assertions.assertEquals(1, successfulDeposits,
                "성공한 입찰 요청 수는 1이어야 합니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + multiAuction.getId());
        redissonClient.getKeys().delete(REDIS_BID_KEY + multiAuction.getId());
    }

    private BidVo generateBid(Auction auction, User user, Integer amount){
        return BidVo.builder()
                .auctionId(auction.getId())
                .userId(user.getId())
                .email(user.getEmail())
                .amount(amount)
                .build();
    }

    private DepositVo generateDeposit(Auction auction, User user, Integer amount){
        return DepositVo.builder()
                .auctionId(auction.getId())
                .userId(user.getId())
                .amount(amount)
                .maxParticipants(auction.getMaxParticipants())
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
                .startAt(now.plusDays(-1))
                .endAt(now.plusDays(2))
                .isEnded(false)
                .build();
    }
}