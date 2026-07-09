package com.example.javatoandroid.websocket;

import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;
import com.example.javatoandroid.BuildConfig;

/**
 * WebSocket 連線配置與工廠
 * 負責獨立管理所有的 WebSocket 連線實體與端點
 */
public class WebSocketClientConfig {

    // Oracle WebSocket 端點 (實機測試根據 IS_LOCAL_TEST 切換)
    public static final String ORACLE_WEBSOCKET_URL = BuildConfig.IS_LOCAL_TEST ? 
            "ws://10.0.2.2:8080/ws/quote" : "ws://161.33.157.67:8088/ws/quote";
    
    private static OkHttpClient webSocketClient = null;

    /**
     * 取得共用的 WebSocket Client (使用 OkHttp)
     */
    public static OkHttpClient getClient() {
        if (webSocketClient == null) {
            webSocketClient = new OkHttpClient.Builder()
                    .pingInterval(10, TimeUnit.SECONDS) // 防呆：定時發送 ping 心跳包保持連線存活
                    .build();
        }
        return webSocketClient;
    }
}
