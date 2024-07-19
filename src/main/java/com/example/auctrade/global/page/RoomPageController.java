package com.example.auctrade.global.page;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/chat")
@Log4j2
public class RoomPageController {
    @GetMapping("/roomList")
    public String roomList(Model model) {
        return "roomList";
    }

    @GetMapping("/room")
    public String getRoom(Model model){
        return "roomDetail";
    }
}
