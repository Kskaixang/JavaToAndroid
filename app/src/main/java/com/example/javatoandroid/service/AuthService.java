package com.example.javatoandroid.service;

import com.example.javatoandroid.repository.AuthRepository;

/**
 * 處理 Android 端的身分驗證與登入業務邏輯
 */
public interface AuthService {
    
    /**
     * 交換 Google Token
     */
    void exchangeGoogleTokenForJwt(String googleIdToken, AuthRepository.AuthCallback callback);

}
