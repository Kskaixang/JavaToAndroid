package com.example.javatoandroid.service;

import com.example.javatoandroid.model.entity.StockData;

/**
 * 獨立的 Service 處理結果回呼介面
 */
public interface StockServiceCallback {
    void onSuccess(StockData data);
    void onError(String errorMessage);
}
