package com.example.auctrade.domain.deposit.service;

import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.deposit.entity.DepositLog;
import com.example.auctrade.domain.deposit.repository.DepositLogRepository;
import com.example.auctrade.domain.deposit.vo.DepositInfoVo;
import com.example.auctrade.domain.deposit.vo.DepositStatus;
import com.example.auctrade.domain.deposit.vo.DepositVo;
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
import org.redisson.api.RScoredSortedSet;
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

import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class DepositServiceImplTest {
    @Autowired
    DepositService depositService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuctionRepository auctionRepository;
    @Autowired
    DepositLogRepository depositLogRepository;
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
        multiAuction = auctionRepository.save(generateAuction(user.getId(),3,3000));

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
        depositLogRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("경매 예치금 등록시 유저 포인트가 부족한 경우 false 값 반환")
    void placeDepositInvalidUserPoint() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 4000;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositService.placeDeposit(generateDeposit(auction, user, AMOUNT)));

        //Then
        assertEquals(ErrorCode.EXCEEDED_POINT_REQUEST.getMessage(), exception.getMessage());

        DepositLog depositLog =  depositLogRepository.findByAuctionIdAndUserId(auction.getId(), user.getId()).orElse(null);
        assertNull(depositLog, "실패한 요청이 DB에 저장 되었습니다.");

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertNull(depositSet.getScore(user.getEmail()), "redis 에 정보가 저장되지 않아야 합니다.");
    }

    @Test
    @DisplayName("경매 예치금 처음 정상 등록하는 경우 true 값 반환 및 redis 정보 저장")
    void placeDepositNewSuccessfulRequest() {
        //Given
        final int BALANCE = 50000;
        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 4000;
        long userId = user.getId();

        //When
        Long result = depositService.placeDeposit(generateDeposit(auction, user, AMOUNT));

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(auction.getId(), depositLog.getAuctionId(), "조회된 경매의 ID가 예상값과 다릅니다."),
                () -> assertEquals(userId, depositLog.getUserId(), "조회된 유저의 ID가 예상값과 다릅니다."),
                () -> assertEquals(AMOUNT, depositLog.getAmount(), "조회된 예치금이 예상값과 다릅니다."),
                () -> assertEquals(DepositStatus.CREATE, depositLog.getStatus(), "조회된 예치금의 상태값이 예상값과 다릅니다..")
        );

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertEquals(AMOUNT, depositSet.getScore(user.getId().toString()), "입력한 예치금 금액이 일치하지 않습니다.");

        Integer userDbPoint = userRepository.findPointByEmail(user.getEmail()).orElse(null);
        assertEquals(BALANCE - AMOUNT, userDbPoint, "유저 포인트가 요청한 금액 만큼 차감되지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("대상 경매 예치금이 가득찬 상태에서 최저 예치금 보다 낮은 금액 요청 시 WRONG_DEPOSIT_CREATE 에러 메시지 발생")
    void placeDepositAtFullAddedInvalidAmount() {
        //Given
        final int BALANCE = 50000;
        User anotherUser = generateUser();
        anotherUser.addPoint(BALANCE);
        anotherUser = userRepository.save(anotherUser);

        Auction auction = auctionRepository.save(generateAuction(anotherUser.getId(),1,3000));
        final int AMOUNT = 4000;
        depositService.placeDeposit(generateDeposit(auction, anotherUser, AMOUNT));

        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);
        User target = user;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositService.placeDeposit(generateDeposit(auction, target, 3500)));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_CREATE.getMessage(), exception.getMessage());

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertNull(depositSet.getScore(target.getId().toString()), "최저 예치금 보다 낮은 입력이 저장되었습니다.");

        Integer anotherUserDbPoint = userRepository.findPointByEmail(anotherUser.getEmail()).orElse(null);
        assertEquals(BALANCE - AMOUNT, anotherUserDbPoint, "유저 포인트가 요청한 금액 만큼 차감되지 않았습니다.");

        Integer userDbPoint = userRepository.findPointByEmail(user.getEmail()).orElse(null);
        assertEquals(BALANCE, userDbPoint, "유저 포인트 잔고가 일치하지 않습니다.");

        assertNull(depositSet.getScore(user.getId().toString()), "실패한 정보가 Redis에 남아있습니다.");
        assertEquals(AMOUNT, depositSet.getScore(anotherUser.getId().toString()), "이전의 정보가 Redis와 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("대상 경매 예치금이 가득찬 상태에서 최저 예치금 보다 높은 금액 요청 시 true 반환 및 데이터 교체")
    void placeDepositAtFullAddedSuccessFulAmount() {
        //Given
        final int BALANCE = 50000;
        User anotherUser = generateUser();
        anotherUser.addPoint(BALANCE);
        anotherUser = userRepository.save(anotherUser);
        Auction auction = auctionRepository.save(generateAuction(anotherUser.getId(),1,3000));

        depositService.placeDeposit(generateDeposit(auction, anotherUser, 4000));

        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);
        User target = user;
        final int AMOUNT = 5000;

        //When
        Long result = depositService.placeDeposit(generateDeposit(auction, target, AMOUNT));

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElse(null);
        assertEquals(DepositStatus.CREATE, depositLog.getStatus(), "정상적인 요청의 상태값이 예상값과 다릅니다.");

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertEquals(AMOUNT, depositSet.getScore(user.getId().toString()), "입력한 예치금 금액이 일치하지 않습니다.");

        Integer anotherUserDbPoint = userRepository.findPointByEmail(anotherUser.getEmail()).orElse(null);
        assertEquals(BALANCE, anotherUserDbPoint, "유저 포인트가 잔고와 일치하지 않습니다.");

        Integer userDbPoint = userRepository.findPointByEmail(user.getEmail()).orElse(null);
        assertEquals(BALANCE - AMOUNT, userDbPoint, "유저 포인트가 요청한 금액 만큼 차감되지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("경매 예치금 2건 이상 정상 등록하는 경우 true 값 반환 및 redis 정보 저장")
    void placeDepositNewSuccessfulRequestMoreThanOne() {
        //Given
        final int BALANCE = 50000;
        List<User> theOthers = new ArrayList<>();
        for(int i = 0 ; i < 2 ;i++){
            User theotherUser = generateUser();
            theotherUser.addPoint(BALANCE);
            theOthers.add(userRepository.save(theotherUser));
        }

        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);
        Auction auction = auctionRepository.save(generateAuction(user.getId()));

        for(User u : theOthers){ depositService.placeDeposit(generateDeposit(auction, u, 4000));}

        //When
        Long result = depositService.placeDeposit(generateDeposit(auction, user, 5000));

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElseThrow();
        assertEquals(DepositStatus.CREATE, depositLog.getStatus(), "정상적인 요청의 상태값이 예상값과 다릅니다.");

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertEquals(theOthers.size()+1, depositSet.size(), "요청 수와 저장된 데이터 수가 일치해야 합니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("예치금이 이미 등록된 경매에서 이전 예치금 보다 낮은 금액 요청 시 WRONG_DEPOSIT_UPDATE 에러 메시지 발생")
    void placeDepositUpdateRequestInvalidAmount() {
        //Given
        final int BALANCE = 50000;
        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);

        final int AMOUNT = 5000;
        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        depositService.placeDeposit(generateDeposit(auction, user, AMOUNT));
        User target = user;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositService.placeDeposit(generateDeposit(auction, target, 3500)));

        //Then
        assertEquals(ErrorCode.WRONG_DEPOSIT_UPDATE.getMessage(), exception.getMessage());

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertEquals(AMOUNT, depositSet.getScore(user.getId().toString()), "입력한 예치금 금액이 일치하지 않습니다.");

        Integer userDbPoint = userRepository.findPointByEmail(user.getEmail()).orElse(null);
        assertEquals(BALANCE - AMOUNT, userDbPoint, "유저 포인트가 요청한 금액 만큼 차감되지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("예치금이 이미 등록된 경매에서 정상 요청 시 true 반환 및 데이터 업데이트")
    void placeDepositSuccessfulUpdateRequest() {
        //Given
        final int BALANCE = 50000;
        User user = generateUser();
        user.addPoint(BALANCE);
        user = userRepository.save(user);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));

        Long beforeResult = depositService.placeDeposit(generateDeposit(auction, user, 4000));
        DepositLog beforeDepositLog = depositLogRepository.findById(beforeResult).orElse(null);
        final int AMOUNT = 5000;

        //When
        Long result = depositService.placeDeposit(generateDeposit(auction, user, AMOUNT));

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElse(null);
        assertEquals(DepositStatus.CANCEL, beforeDepositLog.getStatus(), "이전 요청의 상태값이 예상값과 다릅니다.");
        assertEquals(DepositStatus.CREATE, depositLog.getStatus(), "정상적인 요청의 상태값이 예상값과 다릅니다.");

        RScoredSortedSet<String> depositSet = redissonClient.getScoredSortedSet(REDIS_DEPOSIT_KEY + auction.getId());
        assertEquals(AMOUNT, depositSet.getScore(user.getId().toString()), "입력한 예치금 금액이 일치하지 않습니다.");

        Integer userDbPoint = userRepository.findPointByEmail(user.getEmail()).orElse(null);
        assertEquals(BALANCE - AMOUNT, userDbPoint, "유저 포인트가 요청한 금액 만큼 차감되지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("예치금 입력이 없는 경매에 최소 예치금 정보 조회시 email = NONE, amount -1 반환")
    void getMinDepositInfoWithoutAuctionDeposit() {

        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);
        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        //When
        Integer result = depositService.getMinDepositAmount(auction.getId());

        //Then
        assertEquals(-1, result, "조회된 최소 예치금이 -1이 이납니다.");
    }

    @Test
    @DisplayName("존재하는 예치금이 2개 이상일 경우 최소 예치금 정보 조회시 email 과 amount 반환")
    void getMinDepositInfoAuctionDepositExistMany() {
        //Given
        List<User> theOthers = new ArrayList<>();
        for(int i = 0 ; i < 2 ;i++){
            User theotherUser = generateUser();
            theotherUser.addPoint(50000);
            theOthers.add(userRepository.save(theotherUser));
        }

        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId(),5,3000));
        for(User u : theOthers){ depositService.placeDeposit(generateDeposit(auction, u, 5000)); }
        depositService.placeDeposit(generateDeposit(auction, user, 4000));

        //When
        Integer result = depositService.getMinDepositAmount(auction.getId());

        //Then
        assertEquals(4000, result, "조회된 최소 예치금이 예상값과 다릅니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("존재하지 않는 예치금 취소 요청시 REDIS_INTERNAL_ERROR 메시지 발생")
    void cancelDepositInvalidInfo() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        User target = user;

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositService.cancelDeposit(auction.getId(),target.getId()));

        //Then
        assertEquals(ErrorCode.REDIS_INTERNAL_ERROR.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("정상적인 예치금 취소 요청시 true 반환")
    void cancelDeposit() {
        //Given
        List<User> theOthers = new ArrayList<>();
        for(int i = 0 ; i < 2 ;i++){
            User theotherUser = generateUser();
            theotherUser.addPoint(50000);
            theOthers.add(userRepository.save(theotherUser));
        }

        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
        for(User u :theOthers){ depositService.placeDeposit(generateDeposit(auction, u, 5000)); }

        //When
        Integer result = depositService.getNowParticipants(auction.getId());

        //Then
        assertEquals(theOthers.size(), result, "조회된 예치금 수가 일치하지 않았습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("예치금을 넣지 않은 경매에 특정 회원의 예치금 조회 요청시 0 반환")
    void getDepositAmountNotExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));

        //When
        Integer result = depositService.getDepositAmount(auction.getId(), user.getId());

        //Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("특정 경매의 회원 예치금 조회 요청시 해당 정보 반환")
    void getDepositAmount() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        final int AMOUNT = 5000;
        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        depositService.placeDeposit(generateDeposit(auction, user, AMOUNT));

        //When
        Integer result = depositService.getDepositAmount(auction.getId(), user.getId());

        //Then
        assertEquals(AMOUNT, result, "조회된 유저의 예치금이 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());

    }

    @Test
    @DisplayName("현재 예치금 등록 수 요청시 해당하는 값 반환")
    void getNowParticipants() {
        //Given
        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        depositService.placeDeposit(generateDeposit(auction, user, 5000));

        //When
        Integer result = depositService.getNowParticipants(auction.getId());

        //Then
        assertEquals(1, result, "현재 예치금 등록 인원의 수가 일치하지 않습니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("특정 경매 전체 예치금 정보 조회시 해당하는 값 반환")
    void getAllDepositByAuctionId() {
        //Given
        List<User> theOthers = new ArrayList<>();
        for(int i = 0 ; i < 2 ;i++){
            User theotherUser = generateUser();
            theotherUser.addPoint(50000);
            theOthers.add(userRepository.save(theotherUser));
        }

        User user = generateUser();
        user.addPoint(50000);
        user = userRepository.save(user);

        Auction auction = auctionRepository.save(generateAuction(user.getId(),3,3000));
        final int AMOUNT = 5000;
        for(User u : theOthers){ depositService.placeDeposit(generateDeposit(auction, u, AMOUNT));}

        //When
        List<DepositInfoVo> result = depositService.getAllDepositInfo(auction.getId());

        //Then
        for(int i = 0 ; i < result.size() ; i++){
            assertEquals(theOthers.get(i).getId(), result.get(i).getUserId(), "조회된 유저 ID가 예상값과 다릅니다.");
            assertEquals(AMOUNT, result.get(i).getAmount(), "조회된 최소 예치금이 예상값과 다릅니다.");
        }

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + auction.getId());
    }

    @Test
    @DisplayName("예치금 동시 요청")
    void multiDepositRequest() throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        List<Long> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int finalI = i;

            executor.submit(() -> {
                try {
                    Long id = depositService.placeDeposit(generateDeposit(multiAuction, multiUsers.get(finalI),4000));
                    synchronized (results) {
                        results.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        Assertions.assertEquals(multiAuction.getMaxParticipants(), depositService.getNowParticipants(multiAuction.getId()),
                "성공한 예치금 등록 요청 수는 최대 인원수와 같아야 합니다.");

        int successCount = (int) depositLogRepository.findAllById(results).stream()
                .filter(depositLog -> depositLog.getStatus().equals(DepositStatus.CREATE)).count();

        Assertions.assertEquals(multiAuction.getMaxParticipants(), successCount,
                "성공한 예치금 등록 요청 수는 최대 인원수와 같아야 합니다.");

        redissonClient.getKeys().delete(REDIS_DEPOSIT_KEY + multiAuction.getId());
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
                .startAt(now.plusDays(1))
                .endAt(now.plusDays(2))
                .isEnded(false)
                .build();
    }
}