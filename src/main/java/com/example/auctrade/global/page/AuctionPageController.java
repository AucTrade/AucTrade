package com.example.auctrade.global.page;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/auctions")
public class AuctionPageController {

    // 그리고 여긴가
    @GetMapping("")
    public String roomList() {
        return "auctionMain";
    }

    @GetMapping("/deposits")
    public String depositList() {
        return "auctionMain";
    }

    @GetMapping("/enter")
    public String getRoom(){
        return "auctionRoomDetail";
    }
}
