package com.example.javatoandroid.factory;

import com.example.javatoandroid.service.StockService;
import com.example.javatoandroid.service.impl.StockServiceImpl;

public class StockServiceFactory {
    public static StockService getService() {
        return new StockServiceImpl();
    }
}
