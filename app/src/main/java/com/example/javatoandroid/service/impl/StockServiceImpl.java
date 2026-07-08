package com.example.javatoandroid.service.impl;

import com.example.javatoandroid.model.entity.StockData;
import com.example.javatoandroid.controller.ApiClient;
import com.example.javatoandroid.controller.YahooApiController;
import com.example.javatoandroid.service.StockService;
import com.example.javatoandroid.service.StockServiceCallback;
import com.example.javatoandroid.yahoo.YahooChartResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockServiceImpl implements StockService {
    private YahooApiController yahooApiController;

    public StockServiceImpl() {
        yahooApiController = ApiClient.getYahooClient().create(YahooApiController.class);
    }

    @Override
    public void fetchChartData(String symbol, String interval, String range, StockServiceCallback callback) {
        Call<YahooChartResponse> call = yahooApiController.getChart(symbol, interval, range);
        call.enqueue(new Callback<YahooChartResponse>() {
            @Override
            public void onResponse(Call<YahooChartResponse> call, Response<YahooChartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().chart != null && response.body().chart.result != null) {
                    YahooChartResponse.Result result = response.body().chart.result.get(0);
                    if (result == null || result.meta == null) {
                        callback.onError("回應資料的 Meta 欄位為空");
                        return;
                    }

                    StockData stockData = new StockData();
                    stockData.symbol = result.meta.symbol;
                    stockData.shortName = result.meta.shortName;
                    stockData.currentPrice = result.meta.regularMarketPrice;
                    stockData.previousClose = result.meta.previousClose;

                    if (result.indicators == null || result.indicators.quote == null || result.indicators.quote.isEmpty() || result.timestamp == null || result.timestamp.isEmpty()) {
                        stockData.isChartDataMissing = true;
                    } else {
                        stockData.isChartDataMissing = false;
                        stockData.timestamps = result.timestamp;
                        stockData.opens = result.indicators.quote.get(0).open;
                        stockData.highs = result.indicators.quote.get(0).high;
                        stockData.lows = result.indicators.quote.get(0).low;
                        stockData.closes = result.indicators.quote.get(0).close;
                        
                        // 防護檢查
                        if (stockData.opens == null || stockData.highs == null || stockData.lows == null || stockData.closes == null) {
                            stockData.isChartDataMissing = true;
                        }
                    }

                    callback.onSuccess(stockData);
                } else {
                    callback.onError("伺服器錯誤: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<YahooChartResponse> call, Throwable t) {
                callback.onError("網路連線失敗: " + t.getMessage());
            }
        });
    }
}
