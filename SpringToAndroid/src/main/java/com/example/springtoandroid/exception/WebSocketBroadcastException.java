package com.example.springtoandroid.exception;

/**
 * 自訂義例外：當 WebSocket 發生推播失敗、連線異常時拋出。
 */
public class WebSocketBroadcastException extends RuntimeException {
    public WebSocketBroadcastException(String message) {
        super(message);
    }

    public WebSocketBroadcastException(String message, Throwable cause) {
        super(message, cause);
    }
}
