package com.example.javatoandroid.factory;

import android.content.Context;

import com.example.javatoandroid.BuildConfig;
import com.example.javatoandroid.utils.AuthInterceptor;
import com.example.javatoandroid.utils.TokenStorage;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 負責產生與管理 Retrofit 實體的類別
 * 在此統一設定 OkHttpClient，並掛載 AuthInterceptor 以自動帶上 Token。
 */
public class ApiClient {

    // 依據是否為本機測試切換不同的後端 URL
    private static final String BASE_URL = BuildConfig.IS_LOCAL_TEST ? 
            "http://10.0.2.2:8080/" : "https://your-production-url.com/";

    private static Retrofit retrofit = null;

    /**
     * 取得已設定好 JWT 攔截器的 Retrofit 實體
     * 需要傳入 Context 以初始化 TokenStorage
     */
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            
            // 初始化 Token 儲存工具
            TokenStorage tokenStorage = new TokenStorage(context.getApplicationContext());
            
            // 初始化攔截器
            AuthInterceptor authInterceptor = new AuthInterceptor(tokenStorage);

            // 建立自訂的 OkHttpClient，加入攔截器與超時設定
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            // 建立 Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
