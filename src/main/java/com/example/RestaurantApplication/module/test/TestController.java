package com.example.RestaurantApplication.module.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/sleep")
        public String sleep() throws InterruptedException {
        Thread.sleep(10000);
        return "ok good";
    }
}

