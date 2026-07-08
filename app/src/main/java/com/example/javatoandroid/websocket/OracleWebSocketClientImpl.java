package com.example.javatoandroid.websocket;

import android.util.Log;

import com.example.javatoandroid.exception.WebSocketConnectionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.List;

public class OracleWebSocketClientImpl implements IQuoteWebSocketClient {

    private static final String TAG = "OracleWebSocket";
    private WebSocket webSocket;
    private final OkHttpClient client;
    
    public OracleWebSocketClientImpl() {
        this.client = WebSocketClientConfig.getClient();
    }

    @Override
    public void connect(QuoteListener listener) {
        if (webSocket != null) {
            Log.w(TAG, "WebSocket 已經連線，避免重複建立");
            return;
        }

        Request request = new Request.Builder()
                .url(WebSocketClientConfig.ORACLE_WEBSOCKET_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                Log.i(TAG, "✅ 成功連線至 Oracle WebSocket 伺服器");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                Log.d(TAG, "📥 收到後端推播: " + text);
                try {
                    // 預期格式: "2330:950.0"
                    String[] parts = text.split(":");
                    if (parts.length == 2) {
                        String symbol = parts[0];
                        double price = Double.parseDouble(parts[1]);
                        
                        // 從後端Oracle傳遞給 Listener
                        listener.onQuoteReceived(symbol, price);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析 WebSocket 訊息失敗: " + text, e);
                }
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                Log.i(TAG, "🛑 WebSocket 連線已關閉: " + reason);
                OracleWebSocketClientImpl.this.webSocket = null;
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                Log.e(TAG, "❌ WebSocket 連線異常: ", t);
                OracleWebSocketClientImpl.this.webSocket = null;
                listener.onError(new WebSocketConnectionException("WebSocket 連線異常: " + t.getMessage(), t).getMessage());
            }
        });
    }

    @Override
    public void subscribe(String symbol) {
        if (webSocket != null) {
            webSocket.send("SUBSCRIBE:" + symbol);
            Log.i(TAG, "已送出訂閱請求: " + symbol);
        } else {
            Log.w(TAG, "WebSocket 尚未連線，無法訂閱: " + symbol);
        }
    }

    @Override
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "App主動斷開連線");
            webSocket = null;
            Log.i(TAG, "已手動觸發 WebSocket 斷線");
        }
    }

    @Override
    public void subscribeBatchQuotes(List<String> symbols) {

    }

    @Override
    public void unsubscribeBatchQuotes(List<String> symbols) {

    }
}
