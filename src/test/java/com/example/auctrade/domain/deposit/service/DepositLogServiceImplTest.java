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

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY,
        connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DepositLogServiceImplTest {
    @Autowired
    DepositLogService depositLogService;
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
        depositLogRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("경매 예치금 로그 등록시 해당 ID 반환 및 status CREATE 데이터 저장")
    void createDepositLog() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 4000;
        DepositVo depositVo = generateDeposit(auction, user, AMOUNT);
        //When
        Long result = depositLogService.createDepositLog(depositVo);

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElse(null);
        assertNotNull(depositLog, "데이터가 DB에 저장 되지 않았습니다.");
        assertEquals(depositLog.getId() , result, "반환된 ID 가 일치하지 않습니다.");
        assertEquals(DepositStatus.CREATE, depositLog.getStatus(), "저장된 예치금의 상태가 CREATE 가 아닙니다.");
    }

    @Test
    @DisplayName("잘못된 경매 ID로 경매 예치금 로그 상태 업데이트 요청 시 DEPOSIT_LOG_NOT_FOUND 메시지 발생")
    void updateLogStatusInvalidAuctionId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositLogService.updateLogStatus(-1L, user.getId(), DepositStatus.CANCEL));

        //Then
        assertEquals(ErrorCode.DEPOSIT_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }
    @Test
    @DisplayName("잘못된 유저 ID로 경매 예치금 로그 상태 업데이트 요청 시 DEPOSIT_LOG_NOT_FOUND 메시지 발생")
    void updateLogStatusInvalidUserId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositLogService.updateLogStatus(auction.getId(), -1L, DepositStatus.CANCEL));

        //Then
        assertEquals(ErrorCode.DEPOSIT_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("정상 예치금 로그 상태 업데이트 요청 시 해당 ID 반환")
    void updateLogStatus() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        Long result = depositLogService.updateLogStatus(auction.getId(), user.getId(), DepositStatus.CANCEL);

        //Then
        DepositLog depositLog = depositLogRepository.findById(result).orElse(null);
        assertEquals(DepositStatus.CANCEL, depositLog.getStatus());
    }

    @Test
    @DisplayName("예치금이 없는 경매의 최소 예치금 조회 요청 시 null 반환")
    void getMinDepositLogDepositNotExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));

        //When
        DepositInfoVo result = depositLogService.getMinDepositLog(auction.getId());

        //Then
        assertNull(result, "예치금이 존재하지 않는 경매의 예치금이 조회되었습니다.");
    }

    @Test
    @DisplayName("정상 최소 예치금 조회 요청 시 해당 로그 정보 반환")
    void getMinDepositLog(){
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 5000;
        depositLogService.createDepositLog(generateDeposit(auction, user, AMOUNT));

        //When
        DepositInfoVo result = depositLogService.getMinDepositLog(auction.getId());

        //Then
        Assertions.assertAll(
                () -> assertEquals(auction.getId(), result.getAuctionId(), "조회된 경매의 ID가 예상값과 다릅니다."),
                () -> assertEquals(user.getId(), result.getUserId(), "조회된 유저의 ID가 예상값과 다릅니다."),
                () -> assertEquals(AMOUNT, result.getAmount(), "조회된 예치금이 예상값과 다릅니다.")
        );
    }

    @Test
    @DisplayName("잘못된 경매 ID로 예치금 로그 조회 요청 시 DEPOSIT_LOG_NOT_FOUND 메시지 발생")
    void getDepositLogInvalidAuctionId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositLogService.getDepositLog(-1L, user.getId()));

        //Then
        assertEquals(ErrorCode.DEPOSIT_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("잘못된 유저 ID로 예치금 로그 조회 요청 시 DEPOSIT_LOG_NOT_FOUND 메시지 발생")
    void getDepositLogInvalidUserId() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        CustomException exception = assertThrows(CustomException.class,
                ()-> depositLogService.getDepositLog(auction.getId(), -1L));

        //Then
        assertEquals(ErrorCode.DEPOSIT_LOG_NOT_FOUND.getMessage(), exception.getMessage());
    }


    @Test
    @DisplayName("정상 예치금 로그 조회 요청 시 해당 로그 정보 반환")
    void getDepositLog() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 5000;
        depositLogService.createDepositLog(generateDeposit(auction, user, AMOUNT));

        //When
        DepositInfoVo result = depositLogService.getDepositLog(auction.getId(), user.getId());

        //Then
        Assertions.assertAll(
                () -> assertEquals(auction.getId(), result.getAuctionId(), "조회된 경매의 ID가 예상값과 다릅니다."),
                () -> assertEquals(user.getId(), result.getUserId(), "조회된 유저의 ID가 예상값과 다릅니다."),
                () -> assertEquals(AMOUNT, result.getAmount(), "조회된 예치금이 예상값과 다릅니다.")
        );
    }

    @Test
    @DisplayName("존재하지 않는 로그 데이터 조회 요청 시 false 반환")
    void containsUserIdNotExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        Boolean result = depositLogService.containsUserId(auction.getId(), -1L);

        //Then
        assertFalse(result);
    }

    @Test
    @DisplayName("존재하는 로그 데이터 조회 요청 시 true 반환")
    void containsUserIdExist() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        depositLogService.createDepositLog(generateDeposit(auction, user, 5000));

        //When
        Boolean result = depositLogService.containsUserId(auction.getId(), user.getId());

        //Then
        assertTrue(result);
    }
    @Test
    @DisplayName("특정 경매 예치금 로그 정상 조회 시 로그 정보 리스트 반환")
    void getAllDepositLog() {
        //Given
        User user = userRepository.save(generateUser());
        Auction auction = auctionRepository.save(generateAuction(user.getId()));
        final int AMOUNT = 5000;

        List<User> theOthers = new ArrayList<>();
        for(int i = 0 ; i < 2 ; i++){
            User theOther = userRepository.save(generateUser());
            theOthers.add(theOther);
            depositLogService.createDepositLog(generateDeposit(auction, theOther, AMOUNT));
        }

        //When
        List<DepositInfoVo> result = depositLogService.getAllDepositLog(auction.getId(), DepositStatus.CREATE);

        //Then
        for(int i = 0 ; i < theOthers.size() ; i++){
            assertEquals(theOthers.get(theOthers.size()-1-i).getId() , result.get(i).getUserId());
            assertEquals(AMOUNT, result.get(i).getAmount());
        }
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