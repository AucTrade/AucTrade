package com.example.auctrade.global.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
public class MainPageController {
    @GetMapping("/header")
    public String header(Model model) {
        return "header";
    }

    @GetMapping("/footer")
    public String footer(Model model) {
        return "footer";
    }

    @GetMapping("/")
    public String main() {
        return "auctionBeforeStartList";
    }

    @GetMapping("/mypage")
    public String myPage() {
        return "myPage";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/login")
    public String auth() {
        return "auth";
    }
}
