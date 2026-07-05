package com.example.springtoandroid.controller;

import com.example.springtoandroid.model.entity.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class TimeController {

    @GetMapping("/api/time")
    public ResponseEntity<ApiResponse<String>> getCurrentTime() {
        // 1.指定時區為台北
        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        // 2.取得當前時間
        ZonedDateTime nowInTaipei = ZonedDateTime.now(zoneId);
        // 3.格式化時間輸出
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = nowInTaipei.format(formatter);
        // 4. 將時間包裝進 ApiResponse 中，回傳 200 狀態與成功訊息
        ApiResponse<String> response = ApiResponse.success(200, "時間獲取成功", formattedTime);
        // 5. 回傳封裝好的 ResponseEntity JSON 資料
        return ResponseEntity.ok(response);
    }
}
