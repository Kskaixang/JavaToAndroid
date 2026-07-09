package com.example.javatoandroid.utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP 請求攔截器 (Interceptor)
 * 作用：在每筆打往後端的網路請求送出前，自動把存在 TokenStorage 裡的 JWT
 * 塞進 HTTP Header (Authorization: Bearer <token>) 中。
 */
public class AuthInterceptor implements Interceptor {

    private final TokenStorage tokenStorage;

    public AuthInterceptor(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 取得目前的 Token
        String token = tokenStorage.getToken();

        // 如果 Token 存在，就加入 Authorization Header
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        // 若無 Token (例如尚未登入)，就照原樣發送
        return chain.proceed(originalRequest);
    }
}
