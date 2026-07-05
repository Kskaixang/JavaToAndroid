package com.example.javatoandroid.model.service;

import com.example.javatoandroid.model.entity.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
public interface ApiService {
    // 請求的相對路徑
    @GET("api/time")
    Call<ApiResponse<String>> getCurrentTime();
}