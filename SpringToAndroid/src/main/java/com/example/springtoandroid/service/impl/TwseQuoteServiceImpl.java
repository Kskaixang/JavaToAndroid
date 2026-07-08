package com.example.springtoandroid.service.impl;

import com.example.springtoandroid.config.ApiEndpointConfig;
import com.example.springtoandroid.exception.QuoteFetchException;
import com.example.springtoandroid.service.IStockQuoteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("twseQuoteService")
public class TwseQuoteServiceImpl implements IStockQuoteService {

    private static final Logger log = LoggerFactory.getLogger(TwseQuoteServiceImpl.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TwseQuoteServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getRealTimePrice(String symbol) throws QuoteFetchException {
        String url = ApiEndpointConfig.getTwseUrl(symbol);
        try {
            log.debug("向 TWSE MIS 請求報價: {}", url);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new QuoteFetchException("TWSE 回傳空的內容");
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode msgArray = root.path("msgArray");

            if (msgArray.isArray() && msgArray.size() > 0) {
                JsonNode firstItem = msgArray.get(0);
                String price = firstItem.path("z").asText();
                
                // 有時候剛開盤、撮合中或無交易時，"z" (最新成交價) 可能為空字串或 "-"
                if (price == null || price.trim().isEmpty() || "-".equals(price)) {
                     // 嘗試取最佳賣價 (a) 或 最佳買價 (b) 的第一筆
                     String askStr = firstItem.path("a").asText();
                     String bidStr = firstItem.path("b").asText();
                     
                     if (askStr != null && !askStr.trim().isEmpty() && !"-".equals(askStr)) {
                         price = askStr.split("_")[0];
                     } else if (bidStr != null && !bidStr.trim().isEmpty() && !"-".equals(bidStr)) {
                         price = bidStr.split("_")[0];
                     } else {
                         // 退而求其次取 "y" (昨收價)
                         price = firstItem.path("y").asText();
                     }
                }

                if (price != null && !price.trim().isEmpty() && !"-".equals(price)) {
                    return price;
                }
                throw new QuoteFetchException("TWSE 報價欄位 (z/y) 皆為空");
            } else {
                throw new QuoteFetchException("TWSE 找不到該代碼的報價 (msgArray 為空)");
            }
        } catch (Exception e) {
            log.error("獲取 TWSE 報價失敗: ", e);
            throw new QuoteFetchException("TWSE API 連線或解析失敗: " + e.getMessage(), e);
        }
    }
}
