package com.example.springtoandroid.scheduler;

import com.example.springtoandroid.exception.QuoteFetchException;
import com.example.springtoandroid.factory.QuoteServiceFactory;
import com.example.springtoandroid.service.IStockQuoteService;
import com.example.springtoandroid.websocket.StockWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 報價排程器：每隔固定時間向 TWSE 拿價格，並推播給 App
 */
@Configuration
@EnableScheduling
public class QuoteScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuoteScheduler.class);
    
    private final QuoteServiceFactory quoteServiceFactory;
    private final StockWebSocketHandler webSocketHandler;

    @Autowired
    public QuoteScheduler(QuoteServiceFactory quoteServiceFactory, StockWebSocketHandler webSocketHandler) {
        this.quoteServiceFactory = quoteServiceFactory;
        this.webSocketHandler = webSocketHandler;
    }

    // 每 3 秒執行一次 (3000 ms)
    @Scheduled(fixedRate = 3000)
    public void fetchAndBroadcastQuote() {
        for (String symbol : webSocketHandler.getActiveSymbols()) {
            try {
                IStockQuoteService quoteService = quoteServiceFactory.getPrimaryQuoteService();
                String price = quoteService.getRealTimePrice(symbol);
                
                log.info("【排程器】成功取得 {} 報價: {}，準備推播給移動端...", symbol, price);
                webSocketHandler.broadcast(symbol + ":" + price);
                
            } catch (QuoteFetchException e) {
                log.warn("【排程器】獲取 {} 報價異常，略過本次推播: {}", symbol, e.getMessage());
            } catch (Exception e) {
                log.error("【排程器】發生未預期錯誤: ", e);
            }
        }
    }
}
