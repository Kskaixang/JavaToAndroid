package com.example.javatoandroid.model.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 集中管理所有 API 連線的工廠 (Network Module)。
 * 策略：相同前綴抽取為變數，保持未來大改網址與擴充後端的彈性。
 */
public class ApiClient {

    // Yahoo Finance API 前綴
    private static final String YAHOO_BASE_URL = "https://query1.finance.yahoo.com/";
    
    // 未來的 Oracle 伺服器前綴 (目前先預留)
    private static final String ORACLE_BACKEND_BASE_URL = "http://您的IP或網域:8080/api/";

    private static Retrofit yahooRetrofit = null;
    private static Retrofit backendRetrofit = null;

    /**
     * 取得 Yahoo Finance 的 Retrofit 實例 (包含偽裝瀏覽器的機制)
     */
    public static Retrofit getYahooClient() {
        if (yahooRetrofit == null) {
            // 加入 OkHttpClient 攔截器，偽裝成一般的瀏覽器，避免被 Yahoo 當成機器人阻擋
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request request = chain.request().newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36")
                                .build();
                        return chain.proceed(request);
                    }).build();

            yahooRetrofit = new Retrofit.Builder()
                    .baseUrl(YAHOO_BASE_URL) // 變數帶入前綴
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return yahooRetrofit;
    }

    /**
     * 取得您自己 Oracle 後端的 Retrofit 實例 (目前預留)
     */
    public static Retrofit getBackendClient() {
        if (backendRetrofit == null) {
            backendRetrofit = new Retrofit.Builder()
                    .baseUrl(ORACLE_BACKEND_BASE_URL) // 變數帶入前綴
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return backendRetrofit;
    }
}
