package com.example.javatoandroid.websocket;

import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 連線配置與工廠
 * 負責獨立管理所有的 WebSocket 連線實體與端點
 */
public class WebSocketClientConfig {

    // Oracle WebSocket 端點 (Android 模擬器連本機後端請用 10.0.2.2，若是實機請改成實際 IP)
    public static final String ORACLE_WEBSOCKET_URL = "ws://10.0.2.2:8080/ws/quote";
    
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
