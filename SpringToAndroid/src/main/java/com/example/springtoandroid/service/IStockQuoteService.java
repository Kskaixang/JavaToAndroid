package com.example.springtoandroid.service;

import com.example.springtoandroid.exception.QuoteFetchException;

/**
 * 報價服務介面。
 * 遵守介面分離原則，未來無論擴充 Fugle, Yahoo 均需實作此介面。
 */
public interface IStockQuoteService {
    
    /**
     * 取得指定股票代碼的最新即時價格
     * @param symbol 股票代碼 (例: "2330")
     * @return 價格字串 (如果無資料則拋出例外)
     * @throws QuoteFetchException 當連線異常或解析失敗時拋出
     */
    String getRealTimePrice(String symbol) throws QuoteFetchException;
}
