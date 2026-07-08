package com.example.springtoandroid.factory;

import com.example.springtoandroid.service.IStockQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 報價服務工廠：
 * 根據需求提供對應的報價爬蟲實作。
 * 雖然目前只有 TWSE，但這為未來的擴充保留了彈性。
 */
@Component
public class QuoteServiceFactory {

    private final IStockQuoteService twseQuoteService;

    @Autowired
    public QuoteServiceFactory(@Qualifier("twseQuoteService") IStockQuoteService twseQuoteService) {
        this.twseQuoteService = twseQuoteService;
    }

    /**
     * 取得主要的報價服務 (目前預設為台灣證交所)
     */
    public IStockQuoteService getPrimaryQuoteService() {
        return twseQuoteService;
    }
}
