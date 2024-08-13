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

    @GetMapping("/deposits")
    public String depositList() {
        return "auctionBeforeStartList";
    }

    @GetMapping("/enter")
    public String getRoom(){
        return "auctionRoomDetail";
    }
}
