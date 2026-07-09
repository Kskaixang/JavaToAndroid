package com.example.springtoandroid.controller;

import com.example.springtoandroid.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 處理登入與身分驗證相關的 API 端點
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 接收前端 (Android) 傳來的 Google Token，並回傳本系統的 JWT
     * 
     * 請求格式 (JSON):
     * {
     *   "token": "eyJhbGciOiJSUzI1NiIs..."
     * }
     * 
     * 回傳格式 (JSON):
     * {
     *   "jwt": "eyJhbGciOiJIUzI1NiJ9..."
     * }
     */
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            // 從請求本體中取得 "token" 欄位
            String googleToken = body.get("token");
            if (googleToken == null || googleToken.isEmpty()) {
                return ResponseEntity.badRequest().body("缺少 Google Token");
            }

            // 呼叫 Service 進行驗證並換取 JWT
            String jwt = authService.googleLogin(googleToken);

            // 將 JWT 包裝成 JSON 格式回傳
            Map<String, String> response = new HashMap<>();
            response.put("jwt", jwt);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 若驗證失敗，回傳 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登入失敗: " + e.getMessage());
        }
    }
}
