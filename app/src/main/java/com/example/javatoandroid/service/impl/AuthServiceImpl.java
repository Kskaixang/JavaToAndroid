package com.example.javatoandroid.service.impl;

import android.content.Context;

import com.example.javatoandroid.factory.ApiClient;
import com.example.javatoandroid.repository.AuthRepository;
import com.example.javatoandroid.service.AuthService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Android 端的身分驗證服務實作
 */
public class AuthServiceImpl implements AuthService {

    /**
     * 靜態巢狀介面：定義與後端 Auth 相關的 API 投射
     */
    public interface AuthApi {
        @POST("/api/auth/google-login")
        Call<Map<String, String>> googleLogin(@Body Map<String, String> body);
    }

    // 將 Retrofit 的 API 實例抽成類別的靜態屬性
    public static AuthApi api;

    private final AuthRepository authRepository;

    public AuthServiceImpl(Context context) {
        // 在建立 Service 時，確保 ApiClient 被初始化
        if (api == null) {
            api = ApiClient.getClient(context).create(AuthApi.class);
        }
        this.authRepository = new AuthRepository(context);
    }

    @Override
    public void exchangeGoogleTokenForJwt(String googleIdToken, AuthRepository.AuthCallback callback) {
        // 委託給 Repository 去處理真實的資料流與網路請求
        authRepository.exchangeGoogleTokenForJwt(googleIdToken, callback);
    }
}
