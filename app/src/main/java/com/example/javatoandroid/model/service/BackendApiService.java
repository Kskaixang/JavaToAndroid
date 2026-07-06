package com.example.javatoandroid.model.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 專門負責與 Oracle 後端溝通的 API 介面。
 * 包含帳戶餘額、庫存、未實現損益、下單委託等功能。
 */
public interface BackendApiService {

    // 取得當前使用者的帳戶總餘額
    @GET("user/balance")
    Call<String> getUserAccountBalance();

    // 取得特定股票的目前持有庫存量
    @GET("user/inventory/{symbol}")
    Call<String> getUserInventory(@Path("symbol") String symbol);

    // 取得未實現損益百分比
    @GET("user/unrealizedPnl")
    Call<String> getUnrealizedPnl(@Query("symbol") String symbol);

    // 送出交易委託單
    @POST("order/submit")
    Call<String> submitOrder(@Query("symbol") String symbol, 
                             @Query("action") String action, 
                             @Query("type") String type, 
                             @Query("price") double price, 
                             @Query("quantity") int quantity);
}
