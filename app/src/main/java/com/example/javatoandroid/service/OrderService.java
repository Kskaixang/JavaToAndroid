package com.example.javatoandroid.service;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * 處理 Android 端的訂單業務邏輯
 */
public interface OrderService {

    /**
     * 送出委託下單
     */
    void submitOrder(Map<String, Object> orderData, Callback<Map<String, Object>> callback);
}
