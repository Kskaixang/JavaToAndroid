package com.example.javatoandroid.factory;

import com.example.javatoandroid.websocket.IQuoteWebSocketClient;
import com.example.javatoandroid.websocket.OracleWebSocketClientImpl;

/**
 * WebSocket 客戶端工廠
 * 用來解耦 Fragment 與具體的 WebSocket 實作，方便未來切換連線源或進行單元測試。
 */
public class WebSocketClientFactory {

    private static IQuoteWebSocketClient instance;

    public static IQuoteWebSocketClient getClient() {
        if (instance == null) {
            instance = new OracleWebSocketClientImpl();
        }
        return instance;
    }
}
