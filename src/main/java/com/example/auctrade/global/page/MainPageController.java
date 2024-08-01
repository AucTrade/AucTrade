package com.example.auctrade.global.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
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
        return "auctionList";
    }

    @GetMapping("/login")
    public String auth() {
        return "auth";
    }
}
