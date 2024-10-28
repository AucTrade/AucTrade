package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;
import com.example.auctrade.domain.auction.entity.Auction;
import com.example.auctrade.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.*;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static com.example.auctrade.global.constant.Constants.REDIS_BID_KEY;

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
    RedissonClient redissonClient;
    List<Auction> auctions = new ArrayList<>();
    List<String> buyerList = new ArrayList<>();
    List<BidDTO.Result> createResults = new ArrayList<>();
    private static final String BID_USER_KEY = "username";
    private static final String BID_PRICE_KEY = "bid";

    @BeforeAll
    void createData(){

        for(int i = 0 ; i < 6; i++){
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
                    .maxParticipants(3)
                    .minimumPrice(1000 + i)
                    .productId(i)
                    .sellerEmail("test" + (i % 3) + "@test.com")
                    .build()));
        }
        for(int i = 0 ; i < 10; i++){
            buyerList.add("buyer"+i+"@test.com");
        }

        Auction targetAuction;

        //정상 등록
        for(int i = 0 ; i < 4 ; i++){
            targetAuction = auctions.get(i);
            createResults.add(bidService.placeBid(BidDTO.Create.builder()
                    .auctionId(targetAuction.getId())
                    .username(buyerList.get(i))
                    .price(3000)
                    .build()));
        }

        //추가 등록 낮은 가격
        targetAuction = auctions.get(1);
        createResults.add(bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(4))
                .price(2000)
                .build()));

        //추가 등록 같은 가격
        targetAuction = auctions.get(2);
        createResults.add(bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(5))
                .price(3000)
                .build()));

        //추가 등록 높은 가격
        targetAuction = auctions.get(3);
        createResults.add(bidService.placeBid(BidDTO.Create.builder()
                .auctionId(targetAuction.getId())
                .username(buyerList.get(6))
                .price(5000)
                .build()));
    }

    @AfterAll
    void deleteData(){
        for(Auction auction : auctions){
            RBucket<Object> bucket = redissonClient.getBucket(REDIS_BID_KEY + auction.getId());
            if (bucket.isExists()) bucket.delete();
        }
        for(String email : buyerList){
            bidService.removeMyBidLog(email);
        }
    }

    @Test
    @DisplayName("경매 입찰 성공 여부 확인")
    void placeBid() {
        for(Auction auction : auctions){

            RMap<String, String> bidMap = redissonClient.getMap(REDIS_BID_KEY + auction.getId());
            System.out.println();
            System.out.println(bidMap.getName());
            System.out.println(bidMap.get(BID_USER_KEY));
            System.out.println(bidMap.get(BID_PRICE_KEY));
            System.out.println();
        }

        //0 ~ 3 정상 입찰
        Assertions.assertTrue(createResults.get(3).getIsSuccess());
        //4 현제 입찰가 보다 낮은 입찰
        Assertions.assertFalse(createResults.get(4).getIsSuccess());
        //5 현제 입찰가와 같은 입찰
        Assertions.assertFalse(createResults.get(5).getIsSuccess());
        //6 현제 입찰 보다 높은 입찰
        Assertions.assertTrue(createResults.get(6).getIsSuccess());
        Assertions.assertEquals(buyerList.get(6), createResults.get(6).getUsername());
        Assertions.assertEquals(5000, createResults.get(6).getPrice());
    }

    @Test
    @DisplayName("경매 현재 입찰 정보 조회")
    void getCurrentBid() {
        Assertions.assertEquals(3000, bidService.getCurrentBid(auctions.get(0).getId()).getPrice());
        Assertions.assertEquals(buyerList.get(0), bidService.getCurrentBid(auctions.get(0).getId()).getUsername());

        Assertions.assertEquals(-1, bidService.getCurrentBid(auctions.get(4).getId()).getPrice());
        Assertions.assertEquals("NONE", bidService.getCurrentBid(auctions.get(4).getId()).getUsername());
    }

    @Test
    @DisplayName("경매 현재 입찰금 조회")
    void getBidPrice() {
        Assertions.assertEquals(3000, bidService.getBidPrice(auctions.get(0).getId()));

        Assertions.assertEquals(-1, bidService.getBidPrice(auctions.get(4).getId()));
    }

    @Test
    @DisplayName("경매 현재 입찰 유저 조회")
    void getBidUser() {
        Assertions.assertEquals(buyerList.get(0), bidService.getBidUser(auctions.get(0).getId()));

        Assertions.assertEquals("NONE", bidService.getBidUser(auctions.get(4).getId()));
    }

    @Test
    @DisplayName("경매 현재 입찰 내역 조회")
    void getBidLogs() {
        Assertions.assertEquals(1, bidService.getBidLogs(auctions.get(2).getId()).size());

        Assertions.assertEquals(2, bidService.getBidLogs(auctions.get(3).getId()).size());
    }

    @Test
    @DisplayName("경매 동시 입찰 ")
    void multiBidRequest() throws InterruptedException{
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        List<Boolean> results = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    boolean success = bidService.placeBid(BidDTO.Create.builder()
                            .auctionId(auctions.get(5).getId())
                            .price(2000)
                            .username(buyerList.get(finalI))
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