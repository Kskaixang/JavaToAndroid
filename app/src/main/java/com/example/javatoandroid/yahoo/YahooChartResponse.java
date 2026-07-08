package com.example.javatoandroid.yahoo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class YahooChartResponse {
    @SerializedName("chart")
    public Chart chart;

    public static class Chart {
        @SerializedName("result")
        public List<Result> result;
        
        @SerializedName("error")
        public Object error;
    }

    public static class Result {
        @SerializedName("meta")
        public Meta meta;

        @SerializedName("timestamp")
        public List<Long> timestamp;

        @SerializedName("indicators")
        public Indicators indicators;
    }

    public static class Meta {
        @SerializedName("currency")
        public String currency;

        @SerializedName("symbol")
        public String symbol;

        @SerializedName("shortName")
        public String shortName;

        @SerializedName("regularMarketPrice")
        public double regularMarketPrice;

        @SerializedName("previousClose")
        public double previousClose;
    }

    public static class Indicators {
        @SerializedName("quote")
        public List<Quote> quote;
    }

    public static class Quote {
        @SerializedName("open")
        public List<Double> open;

        @SerializedName("high")
        public List<Double> high;

        @SerializedName("low")
        public List<Double> low;

        @SerializedName("close")
        public List<Double> close;

        @SerializedName("volume")
        public List<Long> volume;
    }
}
