package com.example.javatoandroid.exception;

/**
 * 自訂義例外：當 Android App 無法連上 Oracle 後端 WebSocket 或發生斷線時拋出。
 * 用於取代原本雜亂的 IOException，方便 Log 篩選與錯誤上報。
 */
public class WebSocketConnectionException extends RuntimeException {
    public WebSocketConnectionException(String message) {
        super(message);
    }

    public WebSocketConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
