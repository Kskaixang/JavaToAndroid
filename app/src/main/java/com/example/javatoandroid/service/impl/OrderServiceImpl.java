package com.example.javatoandroid.service.impl;

import android.content.Context;

import com.example.javatoandroid.factory.ApiClient;
import com.example.javatoandroid.service.OrderService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Android 端的訂單服務實作
 */
public class OrderServiceImpl implements OrderService {

    /**
     * 靜態巢狀介面：定義與後端 Order 相關的 API 投射
     */
    public interface OrderApi {
        @POST("/api/order/submit")
        Call<Map<String, Object>> submitOrder(@Body Map<String, Object> orderData);
    }

    // 將 Retrofit 的 API 實例抽成類別的靜態屬性
    public static OrderApi api;

    public OrderServiceImpl(Context context) {
        if (api == null) {
            api = ApiClient.getClient(context).create(OrderApi.class);
        }
    }

    @Override
    public void submitOrder(Map<String, Object> orderData, Callback<Map<String, Object>> callback) {
        api.submitOrder(orderData).enqueue(callback);
    }
}
