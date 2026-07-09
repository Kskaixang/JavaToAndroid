package com.example.springtoandroid.service.impl;

import com.example.springtoandroid.model.entity.User;
import com.example.springtoandroid.repository.UserRepository;
import com.example.springtoandroid.security.JwtUtils;
import com.example.springtoandroid.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * 處理驗證與登入相關業務邏輯的服務層實作
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public String googleLogin(String idTokenString) throws Exception {
        
        // 1. 初始化 Google 的 Token 驗證器，並設定我們專屬的 Web Client ID 作為受眾 (Audience)
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        // 2. 向 Google 驗證此 Token 真偽 (這一步會自動連線到 Google 伺服器檢查簽名)
        GoogleIdToken idToken = verifier.verify(idTokenString);
        
        if (idToken != null) {
            // 驗證成功，從 Payload 提取使用者資訊
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            // subject 就是使用者的唯一 Google ID
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // 3. 去資料庫尋找是否已經有這號人物
            Optional<User> optionalUser = userRepository.findByGoogleId(googleId);
            User user;
            if (optionalUser.isPresent()) {
                // 如果已經存在，我們可以直接取出，或是趁機更新他的大頭貼與名稱
                user = optionalUser.get();
                user.setName(name);
                user.setAvatarUrl(pictureUrl);
                userRepository.save(user); // 更新資料
            } else {
                // 如果不存在，代表是新使用者，我們幫他註冊
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setName(name);
                user.setAvatarUrl(pictureUrl);
                userRepository.save(user); // 寫入資料庫
            }

            // 4. 簽發並回傳屬於本系統的 JWT，這裡我們使用 Google ID 作為 JWT 的 Subject
            // 後續 JwtRequestFilter 就會根據這個 Google ID 來辨識身分
            return jwtUtils.generateToken(user.getGoogleId());
            
        } else {
            // Token 驗證失敗 (可能是偽造的，或者是過期了，或者是 Client ID 不符)
            throw new RuntimeException("無效的 Google ID Token！");
        }
    }
}
