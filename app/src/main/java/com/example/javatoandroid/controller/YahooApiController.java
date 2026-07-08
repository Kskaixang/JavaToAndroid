package com.example.javatoandroid.controller;

import com.example.javatoandroid.yahoo.YahooChartResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YahooApiController {
    // 範例： GET https://query1.finance.yahoo.com/v8/finance/chart/2330.TW?interval=1m&range=1d
    @GET("v8/finance/chart/{symbol}")
    Call<YahooChartResponse> getChart(
        @Path("symbol") String symbol,
        @Query("interval") String interval,
        @Query("range") String range
    );
}
