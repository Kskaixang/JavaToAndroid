package com.example.springtoandroid.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 負責管理 WebSocket 連線與推播
 */
@Component
public class StockWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(StockWebSocketHandler.class);
    
    // 執行緒安全的 Set 來儲存所有連線的 Android 客戶端
    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("新客戶端連線成功: {}, 目前連線總數: {}", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("客戶端中斷連線: {}, 狀態: {}, 目前連線總數: {}", session.getId(), status, sessions.size());
    }

    // 儲存目前有哪些股票被訂閱
    private final Set<String> activeSymbols = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到客戶端 {} 訊息: {}", session.getId(), payload);
        if (payload.startsWith("SUBSCRIBE:")) {
            String symbol = payload.split(":")[1];
            // 因為目前是單機測試 Demo，我們切換股票時直接清空舊的，確保只訂閱一檔
            activeSymbols.clear();
            activeSymbols.add(symbol);
            log.info("切換追蹤股票代碼: {}", symbol);
        }
    }

    public Set<String> getActiveSymbols() {
        if (activeSymbols.isEmpty()) {
            return Collections.singleton("2330");
        }
        return activeSymbols;
    }

    /**
     * 廣播訊息給所有已連線的客戶端
     */
    public void broadcast(String message) {
        if (sessions.isEmpty()) {
            return; // 沒人連線就不白做工
        }
        
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("推播訊息至客戶端 {} 失敗", session.getId(), e);
            }
        }
    }
}
