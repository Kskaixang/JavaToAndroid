package com.example.javatoandroid.websocket;

/**
 * WebSocket 客戶端介面：負責啟動連線與斷開連線
 */
public interface IQuoteWebSocketClient {

    /**
     * 建立與伺服器的 WebSocket 連線
     */
    void connect(QuoteListener listener);

    /**
     * 訂閱特定股票代碼 (單筆)
     * @param symbol 股票代碼 (例: "2330.TW")
     */
    void subscribe(String symbol);

    /**
     * 主動斷開連線
     */
    void disconnect();

    // ==========================================
    // 未來規劃：多檔持股即時報價 (Batch Subscription)
    // ==========================================
    // 當使用者持有多檔股票 (如 5~6 檔) 時，若每檔都建立一條連線或分開發送請求，會耗費過多資源且無法保證同步渲染。
    // 解決方案：在同一個 WebSocket 連線中，發送一個包含多檔股票代號的 JSON Array。
    // 預期 Payload 範例: {"action": "subscribe", "symbols": ["2330.TW", "2317.TW", "2454.TW"]}
    // 伺服器端則會每秒或每分鐘推送一次這些股票的「最新批次報價」，讓首頁/庫存頁面能盡速且同時渲染。
    void subscribeBatchQuotes(java.util.List<String> symbols);

    // 取消訂閱批次股票 (例如使用者賣光了 A 持股，就不該繼續接收 A 的報價)
    // 預期 Payload 範例: {"action": "unsubscribe", "symbols": ["2330.TW"]}
    void unsubscribeBatchQuotes(java.util.List<String> symbols);
}
