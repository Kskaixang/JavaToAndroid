package com.example.javatoandroid.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.javatoandroid.factory.ApiClient;
import com.example.javatoandroid.service.impl.AuthServiceImpl;
import com.example.javatoandroid.utils.TokenStorage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 負責處理登入相關的網路請求與資料儲存
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    
    private final AuthServiceImpl.AuthApi authApi;
    private final TokenStorage tokenStorage;

    public AuthRepository(Context context) {
        // 取得已經掛載攔截器的 Retrofit 實體並建立 API (我們將 Api 放在 AuthServiceImpl 裡)
        this.authApi = AuthServiceImpl.api;
        this.tokenStorage = new TokenStorage(context);
    }

    /**
     * 登入結果的回呼介面
     */
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * 將 Google SDK 給的 idToken 傳送給 Spring Boot 後端換取 JWT
     * 換取成功後，自動存入 TokenStorage
     *
     * @param googleIdToken Google 核發的 ID Token
     * @param callback 回呼介面
     */
    public void exchangeGoogleTokenForJwt(String googleIdToken, AuthCallback callback) {
        
        Map<String, String> body = new HashMap<>();
        body.put("token", googleIdToken);

        authApi.googleLogin(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 後端驗證成功，取得我們自己系統的 JWT
                    String jwt = response.body().get("jwt");
                    if (jwt != null) {
                        // 儲存至 SharedPreferences
                        tokenStorage.saveToken(jwt);
                        Log.d(TAG, "JWT 儲存成功！");
                        callback.onSuccess();
                    } else {
                        callback.onError("後端回傳成功，但未包含 JWT");
                    }
                } else {
                    Log.e(TAG, "後端拒絕了 Google Token: " + response.code());
                    callback.onError("登入失敗，伺服器錯誤代碼: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                Log.e(TAG, "網路連線失敗", t);
                callback.onError("網路連線失敗: " + t.getMessage());
            }
        });
    }
}
