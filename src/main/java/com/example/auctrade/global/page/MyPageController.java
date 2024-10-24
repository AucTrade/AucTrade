package com.example.auctrade.global.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/users")
public class MyPageController {
    @GetMapping("/auctions")
    public String roomList() {
        return "myAuctionList";
    }

    @GetMapping("/auctions/my-list")
    public String myRoomList() {
        return "createAuctionList";
    }

    @GetMapping("/auctions/enter-list")
    public String depositRoomList() {
        return "enterAuctionList";
    }

    @GetMapping("/auctions/end-list")
    public String endRoomList() {
        return "endAuctionList";
    }

    @GetMapping("/deposits")
    public String depositList() {
        return "auctionBeforeStartList";
    }

    @GetMapping("/enter")
    public String getRoom(){
        return "auctionRoomDetail";
    }

    @GetMapping("/limits/my-list")
    public String myLimitedList() {
        return "createLimitedList";
    }

    @GetMapping(value = "/purchases/my-list")
    public String myPurchaseList() {
        return "buyPurchaseList";
    }
}
