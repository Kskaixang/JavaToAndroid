package com.example.javatoandroid.controller;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 專門負責與 Oracle 後端溝通的 API 介面。
 * 包含帳戶餘額、庫存、未實現損益、下單委託等功能。
 */
public interface BackendApiController {

    // ==========================================
    // 1. 使用者個人資產與庫存 (Portfolio)
    // ==========================================

    // 取得當前使用者的總資產狀態 (包含現金餘額、總庫存現值、總未實現損益)
    @GET("user/balance")
    Call<String> getUserAccountBalance();

    // [新增] 取得使用者「所有持股」的完整清單 (Portfolio)
    // 說明：比起一檔一檔查，進入首頁或庫存頁面時，應該一次拉回所有持股的平均成本與數量。
    // 回傳格式預期為 JSON Array: [{symbol: "2330.TW", quantity: 5, avgPrice: 600}, ...]
    @GET("user/portfolio")
    Call<String> getUserPortfolio();

    // 取得特定單一股票的目前持有庫存量 (用於下單頁面的快速檢核)
    @GET("user/inventory/{symbol}")
    Call<String> getUserInventory(@Path("symbol") String symbol);

    // ==========================================
    // 2. 交易委託 (Order)
    // ==========================================

    // 送出交易委託單
    // 未來應改為 @Body 傳遞 JSON，避免 @Query 在 URL 暴露過多交易細節
    @POST("order/submit")
    Call<String> submitOrder(@Query("symbol") String symbol, 
                             @Query("action") String action, 
                             @Query("type") String type, 
                             @Query("price") double price, 
                             @Query("quantity") int quantity);

    // ==========================================
    // 3. 多人競賽與排行 (Leaderboard) - 未來規劃
    // ==========================================

    // [新增] 取得全服玩家投資績效排行榜 (Top N)
    // 說明：回傳按「總資產」或「總報酬率」排序的玩家列表。
    @GET("leaderboard/top")
    Call<String> getLeaderboard(@Query("limit") int limit);

    // [新增] 取得當前使用者在全服的排名與勝率
    @GET("user/ranking")
    Call<String> getUserRanking();
}
