package com.example.javatoandroid.model.entity;

import java.util.List;

/**
 * 統一的股票資料模型。
 * 無論資料來源是 Yahoo、Oracle 還是 Fugle，
 * Repository 都應該將資料轉換成此格式後回傳給 UI。
 */
public class StockData {
    public String symbol;
    public String shortName;
    public double currentPrice;
    public double previousClose;
    public boolean isChartDataMissing; // 若盤初無圖表陣列時為 true

    public List<Long> timestamps;
    public List<Double> opens;
    public List<Double> highs;
    public List<Double> lows;
    public List<Double> closes;
}
