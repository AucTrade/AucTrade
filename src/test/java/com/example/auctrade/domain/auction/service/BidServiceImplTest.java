package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.dto.DepositDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import com.example.auctrade.domain.user.entity.User;
import com.example.auctrade.domain.user.entity.UserRoleEnum;
import com.example.auctrade.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;
import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

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
    AuctionRepository auctionRepository;
    @Autowired
    DepositService depositService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserRepository userRepository;
    List<Auction> auctions = new ArrayList<>();
    List<User> buyerList = new ArrayList<>();

    @BeforeAll
    void createData(){

        for(int i = 0 ; i < 10; i++){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            LocalDateTime endTime;

            startTime = now.minusDays(1);
            endTime = now.plusDays(1);

            auctions.add(auctionRepository.save(Auction.builder()
                    .title("제목" + i)
                    .description("내용" + i)
                    .startTime(startTime)
                    .endTime(endTime)
                    .maxParticipants(10)
                    .minimumPrice(1000 + i)
                    .productId(i)
                    .sellerEmail("test" + (i % 3) + "@test.com")
                    .build()));
        }

        for(int i = 0 ; i < 20; i++){
            buyerList.add(userRepository.save(User.builder()
                    .email("buyer"+i+"@test.com")
                    .birth(LocalDate.now())
                    .phone("010-0000-0000")
                    .address("address"+i)
                    .role(UserRoleEnum.USER)
                    .postcode("12341")
                    .password("123")
                    .build()));
        }
        for (User user : buyerList) {
            user.addPoint(500000);
            userRepository.save(user);
        }
    }

    @AfterAll
    void deleteData(){
        for(Auction auction : auctions){
            RBucket<Object> bidBucket = redissonClient.getBucket(REDIS_BID_KEY + auction.getId());
            if (bidBucket.isExists()) bidBucket.delete();

            RBucket<Object> depositBucket = redissonClient.getBucket(REDIS_DEPOSIT_KEY + auction.getId());
            if (depositBucket.isExists()) depositBucket.delete();
        }
        for(User buyer : buyerList){
            bidService.removeMyBidLog(buyer.getEmail());
            depositService.removeMyDepositLog(buyer.getEmail());
        }
    }

    @Test
    @DisplayName("경매 입찰 성공 여부 확인")
    void placeBid() {
        Auction targetAuction = auctions.get(0);
        for(int i = 0 ; i < 5 ; i++){
            depositService.registerDeposit(DepositDTO.Create.builder()
                    .auctionId(targetAuction.getId())
                    .email(buyerList.get(i).getEmail())
                    .deposit(7000)
                    .maxParticipation(targetAuction.getMaxParticipants())
                    .minimumPrice(targetAuction.getMinimumPrice())
                    .startAt(LocalDateTime.now().plusDays(1).toString()).
                    build());
        }

        //정상 등록
        BidDTO.Result result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(0).getEmail())
                .price(3000)
                .build());

        Assertions.assertTrue(result.getIsSuccess());

        //현재 입찰 보다 낮은 가격
        result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(1).getEmail())
                .price(2000)
                .build());

        Assertions.assertFalse(result.getIsSuccess());

        //현재 입찰과 같은 가격
                result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(2).getEmail())
                .price(3000)
                .build());

        Assertions.assertFalse(result.getIsSuccess());

        //현재 입찰보다 높은 가격
        result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(3).getEmail())
                .price(6000)
                .build());

        Assertions.assertTrue(result.getIsSuccess());

        //현재 입찰보다 높은 가격 (예치금 부족
        result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(4).getEmail())
                .price(10000)
                .build());

        Assertions.assertFalse(result.getIsSuccess());
        Assertions.assertEquals(buyerList.get(3).getEmail(), bidService.getBidUser(targetAuction.getId()));
        Assertions.assertEquals(6000, bidService.getBidPrice(targetAuction.getId()));
    }

    @Test
    @DisplayName("경매 현재 입찰 정보 조회")
    void getCurrentBid() {
        Auction targetAuction = auctions.get(1);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .email(buyerList.get(5).getEmail())
                .deposit(7000)
                .maxParticipation(targetAuction.getMaxParticipants())
                .minimumPrice(targetAuction.getMinimumPrice())
                .startAt(LocalDateTime.now().plusDays(1).toString()).
                build());

        BidDTO.Result result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(5).getEmail())
                .price(3000)
                .build());

        Assertions.assertTrue(result.getIsSuccess());

        Assertions.assertEquals(3000, bidService.getCurrentBid(targetAuction.getId()).getPrice());
        Assertions.assertEquals(buyerList.get(5).getEmail(), bidService.getCurrentBid(targetAuction.getId()).getUsername());

        Assertions.assertEquals(-1, bidService.getCurrentBid(auctions.get(2).getId()).getPrice());
        Assertions.assertEquals("NONE", bidService.getCurrentBid(auctions.get(2).getId()).getUsername());
    }

    @Test
    @DisplayName("경매 현재 입찰금 조회")
    void getBidPrice() {
        Auction targetAuction = auctions.get(3);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .email(buyerList.get(6).getEmail())
                .deposit(7000)
                .maxParticipation(targetAuction.getMaxParticipants())
                .minimumPrice(targetAuction.getMinimumPrice())
                .startAt(LocalDateTime.now().plusDays(1).toString()).
                build());

        BidDTO.Result result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(6).getEmail())
                .price(3000)
                .build());

        Assertions.assertTrue(result.getIsSuccess());
        Assertions.assertEquals(3000, bidService.getBidPrice(targetAuction.getId()));
        Assertions.assertEquals(-1, bidService.getBidPrice(auctions.get(4).getId()));
    }

    @Test
    @DisplayName("경매 현재 입찰 유저 조회")
    void getBidUser() {
        Auction targetAuction = auctions.get(5);

        depositService.registerDeposit(DepositDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .email(buyerList.get(7).getEmail())
                .deposit(7000)
                .maxParticipation(targetAuction.getMaxParticipants())
                .minimumPrice(targetAuction.getMinimumPrice())
                .startAt(LocalDateTime.now().plusDays(1).toString()).
                build());

        BidDTO.Result result = bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(7).getEmail())
                .price(3000)
                .build());

        Assertions.assertTrue(result.getIsSuccess());
        Assertions.assertEquals(buyerList.get(7).getEmail(), bidService.getBidUser(targetAuction.getId()));
        Assertions.assertEquals("NONE", bidService.getBidUser(auctions.get(6).getId()));
    }

    @Test
    @DisplayName("경매 현재 입찰 내역 조회")
    void getBidLogs() {
        Auction targetAuction = auctions.get(7);

        for(int i = 8 ; i < 10 ; i++){
            depositService.registerDeposit(DepositDTO.Create.builder()
                    .auctionId(targetAuction.getId())
                    .email(buyerList.get(i).getEmail())
                    .deposit(7000)
                    .maxParticipation(targetAuction.getMaxParticipants())
                    .minimumPrice(targetAuction.getMinimumPrice())
                    .startAt(LocalDateTime.now().plusDays(1).toString()).
                    build());

            bidService.placeBid(BidDTO.Create.builder()
                    .auctionId(targetAuction.getId())
                    .username(buyerList.get(i).getEmail())
                    .price(3000 + i)
                    .build());
        }

        Assertions.assertEquals(2, bidService.getBidLogs(targetAuction.getId()).size());
    }

    @Test
    @DisplayName("경매 동시 입찰 ")
    void multiBidRequest() throws InterruptedException{
        Auction targetAuction = auctions.get(8);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        List<Boolean> results = new ArrayList<>();

        for(int i = 10 ; i < 20 ; i++) {
            depositService.registerDeposit(DepositDTO.Create.builder()
                    .auctionId(targetAuction.getId())
                    .email(buyerList.get(i).getEmail())
                    .deposit(7000)
                    .maxParticipation(targetAuction.getMaxParticipants())
                    .minimumPrice(targetAuction.getMinimumPrice())
                    .startAt(LocalDateTime.now().plusDays(1).toString()).
                    build());
        }

        for (int i = 10; i < 20; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    boolean success = bidService.placeBid(BidDTO.Create.builder()
                            .auctionId(targetAuction.getId())
                            .price(2000)
                            .username(buyerList.get(finalI).getEmail())
                            .build()).getIsSuccess();

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

        long successfulBid = results.stream().filter(Boolean::booleanValue).count();
        Assertions.assertEquals(10, results.size());
        Assertions.assertEquals(1,successfulBid);
    }
}