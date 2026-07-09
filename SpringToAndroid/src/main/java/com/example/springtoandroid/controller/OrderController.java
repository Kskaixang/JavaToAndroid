package com.example.springtoandroid.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @PostMapping("/submit")
    public Map<String, Object> submitOrder(@RequestBody Map<String, Object> orderData) {
        // 從 Spring Security 上下文中取得經過 JWT 解析後的使用者身分 (通常是 Email 或 ID)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (auth != null && auth.isAuthenticated()) ? auth.getName() : "未登入(匿名)";

        System.out.println("\n========== 收到新的下單委託 ==========");
        System.out.println("身份驗證狀態: " + (auth != null && auth.isAuthenticated()));
        System.out.println("下單用戶 (Token 解析): " + userEmail);
        System.out.println("前端傳來的委託快照資訊: " + orderData);
        System.out.println("=====================================\n");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "訂單已接收");
        response.put("user", userEmail);
        response.put("received_order", orderData);
        return response;
    }
}
