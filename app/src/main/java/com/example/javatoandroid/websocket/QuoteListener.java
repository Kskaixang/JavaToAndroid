package com.example.javatoandroid.websocket;

/**
 * 用來接收 WebSocket 即時報價的回呼介面
 */
public interface QuoteListener {
    /**
     * 當收到最新報價時觸發
     * @param symbol 股票代碼 (例 "2330")
     * @param price 最新成交價
     */
    void onQuoteReceived(String symbol, double price);

    /**
     * 當發生連線錯誤或異常時觸發
     */
    void onError(String errorMessage);
}
