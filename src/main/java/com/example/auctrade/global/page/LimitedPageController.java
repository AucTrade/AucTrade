package com.example.auctrade.global.page;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/limits")
public class LimitedPageController {
    @GetMapping("")
    public String productList() {
        return "limitedSaleList";
    }

    @GetMapping("/details")
    public String limitedDetail(){
        return "limitedDetail";
    }

}
