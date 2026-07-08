package com.example.springtoandroid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 設定檔
 * 負責註冊 WebSocket 端點，供 Android App 連線
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final com.example.springtoandroid.websocket.StockWebSocketHandler stockWebSocketHandler;
    
    @org.springframework.beans.factory.annotation.Autowired
    public WebSocketConfig(com.example.springtoandroid.websocket.StockWebSocketHandler stockWebSocketHandler) {
        this.stockWebSocketHandler = stockWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 註冊 ws://domain/ws/quote 作為連線端點，並允許跨域
        registry.addHandler(stockWebSocketHandler, "/ws/quote").setAllowedOrigins("*");
    }
}
