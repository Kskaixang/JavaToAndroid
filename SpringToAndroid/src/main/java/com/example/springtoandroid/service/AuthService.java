package com.example.springtoandroid.service;

/**
 * 處理驗證與登入相關業務邏輯的服務層介面
 */
public interface AuthService {
    
    /**
     * 處理 Google 登入流程：
     *
     * @param idTokenString 來自移動端 (Android) 獲取的 Google ID Token
     * @return 系統簽發的 JWT 字串
     * @throws Exception 如果 Token 驗證失敗則拋出異常
     */
    String googleLogin(String idTokenString) throws Exception;
}
