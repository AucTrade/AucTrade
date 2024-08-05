package com.example.auctrade.global.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
public class MainPageController {
    @GetMapping("/pages/header")
    public String header(Model model) {
        return "header";
    }

    @GetMapping("/pages/footer")
    public String footer(Model model) {
        return "footer";
    }

    @GetMapping("/")
    public String main() {
        return "auctionList";
    }

    @GetMapping("/pages/login")
    public String auth() {
        return "auth";
    }
}
