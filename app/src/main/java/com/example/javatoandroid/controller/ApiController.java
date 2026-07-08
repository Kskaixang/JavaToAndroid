package com.example.javatoandroid.controller;

import com.example.javatoandroid.model.entity.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
public interface ApiController {
    // 請求的相對路徑
    @GET("api/time")
    Call<ApiResponse<String>> getCurrentTime();
}