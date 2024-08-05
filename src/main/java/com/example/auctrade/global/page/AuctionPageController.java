package com.example.auctrade.global.page;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/pages/auctions")
public class AuctionPageController {
    @GetMapping("")
    public String roomList() {
        return "auctionList";
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
