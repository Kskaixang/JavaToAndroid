package com.example.javatoandroid.service;

import com.example.javatoandroid.model.entity.StockData;

public interface StockService {
    /**
     * @param callback 必須使用 Callback 回傳資料，因為 Android 嚴格禁止在主執行緒 (UI Thread) 執行網路請求。
     */
    void fetchChartData(String symbol, String interval, String range, StockServiceCallback callback);
}
