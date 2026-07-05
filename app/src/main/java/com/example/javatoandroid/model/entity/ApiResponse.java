package com.example.javatoandroid.model.entity;

// 建立 Server 與 Client 在傳遞資料上的統一結構與標準(含錯誤)
public class ApiResponse<T> {

    private int status;     // 狀態 例如: 200-OK
    private String message; // 訊息 例如: 查詢成功, 新增成功, 請求錯誤
    private T data; 	    // payload 實際資料

    // 1. 無參數建構子 (No-args constructor)
    public ApiResponse() {
    }

    // 2. 全參數建構子 (All-args constructor)
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 成功回應的靜態建構方法
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<T>(status, message, data);
    }

    // 失敗回應的靜態建構方法
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<T>(status, message, null);
    }

    // 3. 手動寫出 Getter 與 Setter，確保 Android 端免設定 Lombok 也能 100% 編譯成功
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}