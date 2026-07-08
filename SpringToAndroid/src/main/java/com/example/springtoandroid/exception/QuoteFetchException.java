package com.example.springtoandroid.exception;

/**
 * 自訂義例外：當從外部來源 (如 TWSE, Yahoo) 獲取報價失敗時拋出。
 * 用於精確定位網路異常或 JSON 解析失敗。
 */
public class QuoteFetchException extends RuntimeException {
    public QuoteFetchException(String message) {
        super(message);
    }

    public QuoteFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
