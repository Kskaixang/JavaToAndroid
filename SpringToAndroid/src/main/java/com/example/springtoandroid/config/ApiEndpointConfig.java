package com.example.springtoandroid.config;

import org.springframework.context.annotation.Configuration;

/**
 * 集中管理所有外部 API 的端點 (Endpoints)
 */
@Configuration
public class ApiEndpointConfig {

    // 台灣證交所 MIS API 端點
    public static final String TWSE_MIS_URL = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_%s.tw&json=1&delay=0";
    
    // 未來可以擴充 Fugle 等端點...
    // public static final String FUGLE_API_URL = "https://api.fugle.tw/...";

    public static String getTwseUrl(String symbol) {
        // 移除 Android 傳來的 ".TW" 後綴 (例如 "2330.TW" 變成 "2330")
        String rawSymbol = symbol.replace(".TW", "").replace(".TWO", "");
        return String.format(TWSE_MIS_URL, rawSymbol);
    }
}
