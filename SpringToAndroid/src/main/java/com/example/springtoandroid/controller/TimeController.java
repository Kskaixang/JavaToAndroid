package com.example.springtoandroid.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class TimeController {

    @GetMapping("/api/time")
    public String getCurrentTime() {
        // 1.指定時區為台北
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        // 2.取得當前時間
        ZonedDateTime nowInTaipei = ZonedDateTime.now(zoneId);
        // 3.格式化時間輸出
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 4.回傳時間
        return "當前台北時間 (TW Time): " + nowInTaipei.format(formatter);
    }
}
