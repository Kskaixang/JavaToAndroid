package com.example.javatoandroid.ui.fragment;

import com.example.javatoandroid.R;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.javatoandroid.model.entity.StockData;
import com.example.javatoandroid.factory.StockServiceFactory;
import com.example.javatoandroid.service.StockService;
import com.example.javatoandroid.websocket.IQuoteWebSocketClient;
import com.example.javatoandroid.websocket.QuoteListener;
import com.example.javatoandroid.factory.WebSocketClientFactory;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 股票 K 線與下單面板 Fragment (防閃退與偵錯日誌加強版)
 */
public class StockChartFragment extends Fragment implements QuoteListener {

    private static final String TAG = "StockChartFragment";

    // 頂部 UI
    private TextView tab1D, tabDay, tabWeek, tabMonth;
    private CombinedChart candleChart;

    // 十字指針數據區 (已美化)
    private TextView tvStatsPricePercent;
    private TextView tvStatsCoordinates;
    private TextView btnResetCrosshair;

    // 下單面板 UI
    private TextView tvUnrealizedPnl, tvInventory, tvAccountBalance, tvEstBalance, tvEstInventory;
    private TextView tabBuy, tabSell, tabLimit, tabMarket;
    private Button btnOrderReport, btnSubmitOrder;
    private EditText etPrice, etQuantity;
    private Button btnMinusPrice, btnPlusPrice, btnMinusQuantity, btnPlusQuantity, btnPlus10Quantity, btnFetchPrice;

    private StockService stockService;
    private String currentSymbol = "2330.TW"; 
    private String currentInterval = "1m";

    // 模擬餘額與庫存
    private double mockBalance = 1000000.0;
    private int mockInventory = 5;
    private boolean isBalanceHidden = true; 
    private boolean isBuyAction = true;

    // WebSocket 客戶端
    private IQuoteWebSocketClient webSocketClient;

    // 歷史資料快取 (加強防空指標防護)
    private List<Long> currentTimestamps = new ArrayList<>();
    private List<Double> currentCloses = new ArrayList<>();
    private double firstPriceBaseline = 0.0; 
    private double todayPreviousClose = 0.0; // 今日前一日收盤價 (昨收)，供大字現價區固定顯示今日漲跌

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 開始載入 Fragment 佈局與元件綁定");
        View view = inflater.inflate(R.layout.fragment_stock_chart, container, false);

        try {
            // 綁定圖表與標籤
            candleChart = view.findViewById(R.id.candleChart);
            tab1D = view.findViewById(R.id.tab1D);
            tabDay = view.findViewById(R.id.tabDay);
            tabWeek = view.findViewById(R.id.tabWeek);
            tabMonth = view.findViewById(R.id.tabMonth);

            // 綁定即時指標與重置按鈕
            tvStatsPricePercent = view.findViewById(R.id.tvStatsPricePercent);
            tvStatsCoordinates = view.findViewById(R.id.tvStatsCoordinates);
            btnResetCrosshair = view.findViewById(R.id.btnResetCrosshair);

            // 綁定下單面板元件
            tvUnrealizedPnl = view.findViewById(R.id.tvUnrealizedPnl);
            tvInventory = view.findViewById(R.id.tvInventory);
            tvAccountBalance = view.findViewById(R.id.tvAccountBalance);
            tvEstBalance = view.findViewById(R.id.tvEstBalance);
            tvEstInventory = view.findViewById(R.id.tvEstInventory);
            
            tabBuy = view.findViewById(R.id.tabBuy);
            tabSell = view.findViewById(R.id.tabSell);
            tabLimit = view.findViewById(R.id.tabLimit);
            tabMarket = view.findViewById(R.id.tabMarket);
            
            btnOrderReport = view.findViewById(R.id.btnOrderReport);
            btnSubmitOrder = view.findViewById(R.id.btnSubmitOrder);
            etPrice = view.findViewById(R.id.etPrice);
            etQuantity = view.findViewById(R.id.etQuantity);
            btnMinusPrice = view.findViewById(R.id.btnMinusPrice);
            btnPlusPrice = view.findViewById(R.id.btnPlusPrice);
            btnMinusQuantity = view.findViewById(R.id.btnMinusQuantity);
            btnPlusQuantity = view.findViewById(R.id.btnPlusQuantity);
            btnPlus10Quantity = view.findViewById(R.id.btnPlus10Quantity);
            btnFetchPrice = view.findViewById(R.id.btnFetchPrice);

            stockService = StockServiceFactory.getService();
            webSocketClient = WebSocketClientFactory.getClient();

            setupChart();
            setupListeners();
            setupOrderPanelListeners();
            
            updateBalanceDisplay();
            updateEstimates();

            Log.d(TAG, "onCreateView: UI 初始化成功，開始加載初始股票 " + currentSymbol);
            loadStockData(currentSymbol, "1m", "1d");

        } catch (Exception e) {
            Log.e(TAG, "onCreateView 發生嚴重錯誤(可能是 XML ID 綁定失敗): ", e);
            Toast.makeText(getContext(), "介面綁定異常: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webSocketClient != null) {
            webSocketClient.connect(this);
            String rawCurrent = currentSymbol.replace(".TW", "").replace(".TWO", "");
            webSocketClient.subscribe(rawCurrent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

    @Override
    public void onQuoteReceived(String symbol, double price) {
        if (getActivity() == null || !isAdded()) return;
        
        // 過濾掉非目前觀看的股票代碼
        String rawCurrent = currentSymbol.replace(".TW", "").replace(".TWO", "");
        if (!symbol.equals(rawCurrent)) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            try {
                // 1. 更新大字報價與漲跌幅
                double basePrice = todayPreviousClose > 0 ? todayPreviousClose : price;
                double change = price - basePrice;
                double percent = basePrice > 0 ? (change / basePrice) * 100 : 0.0;
                String sign = change > 0 ? "+" : "";
                String colorStr = change > 0 ? "#F44336" : (change < 0 ? "#4CAF50" : "#E0E0E0");
                
                // 特別標記 [即時] 取代 [延遲20分]
                String formattedText = String.format("<font color='%s'>%.2f (%s%.2f%%)</font> <font color='#FFEB3B'><small>[即時]</small></font>", colorStr, price, sign, percent);
                tvStatsPricePercent.setText(android.text.Html.fromHtml(formattedText, android.text.Html.FROM_HTML_MODE_COMPACT));
                
                // 更新下單價格框
                if (!etPrice.hasFocus()) {
                    etPrice.setText(String.format("%.2f", price));
                }

                // 2. 神不知鬼不覺地替換最後一根 K 線數值
                if (currentCloses != null && !currentCloses.isEmpty() && candleChart.getData() != null) {
                    int lastIndex = currentCloses.size() - 1;
                    currentCloses.set(lastIndex, price); // 更新快取
                    
                    if ("1m".equals(currentInterval)) {
                        LineData lineData = candleChart.getData().getLineData();
                        if (lineData != null && lineData.getDataSetCount() > 0) {
                            ILineDataSet dataSet = lineData.getDataSetByIndex(0);
                            if (dataSet.getEntryCount() > lastIndex) {
                                Entry lastEntry = dataSet.getEntryForIndex(lastIndex);
                                lastEntry.setY((float) price);
                                // 判斷顏色
                                int newColor = price >= firstPriceBaseline ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
                                if (dataSet instanceof LineDataSet) {
                                    List<Integer> colors = ((LineDataSet) dataSet).getColors();
                                    if (colors != null && colors.size() > lastIndex) {
                                        colors.set(lastIndex, newColor);
                                    }
                                }
                            }
                        }
                    } else {
                        CandleData candleData = candleChart.getData().getCandleData();
                        if (candleData != null && candleData.getDataSetCount() > 0) {
                            CandleDataSet dataSet = (CandleDataSet) candleData.getDataSetByIndex(0);
                            if (dataSet.getEntryCount() > lastIndex) {
                                CandleEntry lastEntry = dataSet.getEntryForIndex(lastIndex);
                                lastEntry.setClose((float) price);
                                
                                // 動態更新最高最低
                                if (price > lastEntry.getHigh()) lastEntry.setHigh((float) price);
                                if (price < lastEntry.getLow()) lastEntry.setLow((float) price);
                            }
                        }
                    }
                    
                    // 通知圖表刷新
                    candleChart.getData().notifyDataChanged();
                    candleChart.notifyDataSetChanged();
                    candleChart.invalidate();
                }

            } catch (Exception e) {
                Log.e(TAG, "處理 WebSocket 即時報價時發生錯誤", e);
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "WebSocket 錯誤: " + errorMessage);
    }

    public void setStockSymbol(String symbol) {
        Log.d(TAG, "setStockSymbol: 切換股票目標 ➔ " + symbol);
        String rawSymbol = symbol.split(" ")[0];
        if (!rawSymbol.toUpperCase().endsWith(".TW") && !rawSymbol.toUpperCase().endsWith(".TWO")) {
            this.currentSymbol = rawSymbol + ".TW";
        } else {
            this.currentSymbol = rawSymbol.toUpperCase();
        }
        
        mockInventory = (int)(Math.random() * 10);
        updateBalanceDisplay();
        updateEstimates();
        
        // 重新訂閱 WebSocket
        if (webSocketClient != null) {
            webSocketClient.subscribe(rawSymbol);
        }
        
        if (currentInterval.equals("1m")) loadStockData(currentSymbol, "1m", "1d");
        else if (currentInterval.equals("1d")) loadStockData(currentSymbol, "1d", "3mo");
        else if (currentInterval.equals("1wk")) loadStockData(currentSymbol, "1wk", "1y");
        else if (currentInterval.equals("1mo")) loadStockData(currentSymbol, "1mo", "5y");
    }

    private void setupListeners() {
        tab1D.setOnClickListener(v -> switchTab(tab1D, "1m", "1d"));
        tabDay.setOnClickListener(v -> switchTab(tabDay, "1d", "3mo"));
        tabWeek.setOnClickListener(v -> switchTab(tabWeek, "1wk", "1y"));
        tabMonth.setOnClickListener(v -> switchTab(tabMonth, "1mo", "5y"));

        // 重置十字指針：重新指向最後一根 K 線，維持指針永不消失 (動態搜尋 Highlight DataSet 解決失效問題)
        btnResetCrosshair.setOnClickListener(v -> {
            Log.d(TAG, "onClick: 觸發十字指針重置");
            try {
                if (currentCloses != null && !currentCloses.isEmpty()) {
                    int lastIndex = currentCloses.size() - 1;
                    
                    highlightLatestEntry(lastIndex);
                    
                    // 同步更新座標文字顯示為最新收盤價格
                    Double latestVal = currentCloses.get(lastIndex);
                    if (latestVal != null) {
                        updateCoordinatesText(latestVal);
                    }
                    
                    candleChart.invalidate(); // 強制重繪以顯示十字線
                    Log.d(TAG, "重置指針成功，位置: " + lastIndex);
                } else {
                    Log.w(TAG, "重置失敗: 目前無收盤價資料");
                }
            } catch (Exception e) {
                Log.e(TAG, "重置按鈕執行錯誤: ", e);
            }
        });
    }

    private void switchTab(TextView selectedTab, String interval, String range) {
        TextView[] tabs = {tab1D, tabDay, tabWeek, tabMonth};
        for (TextView tab : tabs) {
            tab.setBackgroundColor(Color.TRANSPARENT);
            tab.setTextColor(Color.parseColor("#A0A0A0"));
        }
        selectedTab.setBackgroundColor(Color.parseColor("#2196F3"));
        selectedTab.setTextColor(Color.WHITE);
        
        currentInterval = interval;
        loadStockData(currentSymbol, interval, range);
    }

    private void setupOrderPanelListeners() {
        View.OnClickListener toggleVisibilityListener = v -> {
            isBalanceHidden = !isBalanceHidden;
            updateBalanceDisplay();
            updateEstimates();
        };
        tvUnrealizedPnl.setOnClickListener(toggleVisibilityListener);
        tvAccountBalance.setOnClickListener(toggleVisibilityListener);
        tvInventory.setOnClickListener(toggleVisibilityListener);
        tvEstBalance.setOnClickListener(toggleVisibilityListener);
        tvEstInventory.setOnClickListener(toggleVisibilityListener);

        tabBuy.setOnClickListener(v -> {
            isBuyAction = true;
            tabBuy.setBackgroundColor(Color.parseColor("#F44336"));
            tabBuy.setTextColor(Color.WHITE);
            tabSell.setBackgroundColor(Color.TRANSPARENT);
            tabSell.setTextColor(Color.parseColor("#A0A0A0"));
            btnSubmitOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")));
            updateEstimates();
        });
        tabSell.setOnClickListener(v -> {
            isBuyAction = false;
            tabSell.setBackgroundColor(Color.parseColor("#4CAF50"));
            tabSell.setTextColor(Color.WHITE);
            tabBuy.setBackgroundColor(Color.TRANSPARENT);
            tabBuy.setTextColor(Color.parseColor("#A0A0A0"));
            btnSubmitOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            updateEstimates();
        });

        tabLimit.setOnClickListener(v -> {
            tabLimit.setBackgroundColor(Color.parseColor("#555555"));
            tabLimit.setTextColor(Color.WHITE);
            tabMarket.setBackgroundColor(Color.TRANSPARENT);
            tabMarket.setTextColor(Color.parseColor("#A0A0A0"));
        });
        tabMarket.setOnClickListener(v -> {
            tabMarket.setBackgroundColor(Color.parseColor("#555555"));
            tabMarket.setTextColor(Color.WHITE);
            tabLimit.setBackgroundColor(Color.TRANSPARENT);
            tabLimit.setTextColor(Color.parseColor("#A0A0A0"));
        });

        btnMinusPrice.setOnClickListener(v -> adjustValue(etPrice, -0.5, true));
        btnPlusPrice.setOnClickListener(v -> adjustValue(etPrice, 0.5, true));
        btnMinusQuantity.setOnClickListener(v -> adjustValue(etQuantity, -1.0, false));
        btnPlusQuantity.setOnClickListener(v -> adjustValue(etQuantity, 1.0, false));
        btnPlus10Quantity.setOnClickListener(v -> adjustValue(etQuantity, 10.0, false));
        btnFetchPrice.setOnClickListener(v -> {
            try {
                if (currentCloses != null && !currentCloses.isEmpty()) {
                    Double latestVal = currentCloses.get(currentCloses.size() - 1);
                    if (latestVal != null) {
                        etPrice.setText(String.format("%.2f", latestVal));
                    }
                }
            } catch (Exception e) {}
        });

        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateEstimates(); }
        };
        etPrice.addTextChangedListener(tw);
        etQuantity.addTextChangedListener(tw);

        btnSubmitOrder.setOnClickListener(v -> {
            try {
                String stockSymbol = currentSymbol;
                String tradeType = isBuyAction ? "BUY" : "SELL";
                double price = Double.parseDouble(etPrice.getText().toString());
                int quantity = Integer.parseInt(etQuantity.getText().toString());
                
                String mockPostJson = String.format(
                    "{\"symbol\":\"%s\",\"type\":\"%s\",\"price\":%.2f,\"quantity\":%d}",
                    stockSymbol, tradeType, price, quantity
                );

                Toast.makeText(getContext(), "送出訂單 JSON:\n" + mockPostJson, Toast.LENGTH_LONG).show();
                btnOrderReport.setText("前筆委託: 已送出");
            } catch (Exception e) {
                Toast.makeText(getContext(), "請輸入正確的價格與數量", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adjustValue(EditText editText, double amount, boolean isDecimal) {
        try {
            double currentVal = Double.parseDouble(editText.getText().toString());
            double newVal = currentVal + amount;
            if (isDecimal) {
                if (newVal < 0) newVal = 0;
                editText.setText(String.format("%.2f", newVal));
            } else {
                if (newVal < 1) newVal = 1; // 數量最少為 1 股/單位，不可為 0 或負數！
                editText.setText(String.valueOf((int)newVal));
            }
        } catch (NumberFormatException e) {
            editText.setText(isDecimal ? "0.0" : "1");
        }
    }

    private void updateBalanceDisplay() {
        if (isBalanceHidden) {
            tvUnrealizedPnl.setText("***");
            tvAccountBalance.setText("*****");
            tvInventory.setText("*****");
        } else {
            tvUnrealizedPnl.setText("0"); // 移除 $ 符號，節省版面空間
            tvAccountBalance.setText(String.format("%,.0f", mockBalance)); // 移除 $ 符號，節省版面空間
            tvInventory.setText(String.valueOf(mockInventory));
        }
    }

    private void updateEstimates() {
        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            int qty = Integer.parseInt(etQuantity.getText().toString());
            double totalCost = price * qty * 1000; 
            
            double estBalance;
            int estInventory;
            if (isBuyAction) {
                estBalance = mockBalance - totalCost;
                estInventory = mockInventory + qty;
            } else {
                estBalance = mockBalance + totalCost;
                estInventory = mockInventory - qty;
            }
            
            if (isBalanceHidden) {
                tvEstBalance.setText("*****");
                tvEstInventory.setText("*****");
            } else {
                tvEstBalance.setText(String.format("%,.0f", estBalance)); // 移除 $ 符號，節省版面空間
                tvEstInventory.setText(String.valueOf(estInventory));
            }
            
            if (estBalance < 0 || estInventory < 0) {
                tvEstBalance.setTextColor(Color.parseColor("#F44336")); 
                tvEstInventory.setTextColor(Color.parseColor("#F44336"));
            } else {
                tvEstBalance.setTextColor(Color.parseColor("#03A9F4")); 
                tvEstInventory.setTextColor(Color.parseColor("#03A9F4"));
            }

            // 🎯 上鎖機制：對比邏輯關係與狀態控制 (數量不可為0、餘額不足或庫存不足時禁用按鈕)
            boolean isValid = true;
            String errorMsg = "";

            if (qty < 1) {
                isValid = false;
                errorMsg = "數量不可為0";
            } else if (isBuyAction) {
                if (estBalance < 0) {
                    isValid = false;
                    errorMsg = "餘額不足";
                }
            } else {
                if (estInventory < 0) {
                    isValid = false;
                    errorMsg = "庫存不足";
                }
            }

            if (!isValid) {
                btnSubmitOrder.setEnabled(false);
                btnSubmitOrder.setText(errorMsg);
                btnSubmitOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#424242"))); // 灰色上鎖狀態
            } else {
                btnSubmitOrder.setEnabled(true);
                btnSubmitOrder.setText("確認下單");
                int activeColor = isBuyAction ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
                btnSubmitOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor)); // 恢復紅/綠正常狀態
            }

        } catch (Exception e) {
            tvEstBalance.setText("--");
            tvEstInventory.setText("--");
            btnSubmitOrder.setEnabled(false);
            btnSubmitOrder.setText("確認下單");
            btnSubmitOrder.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#424242")));
        }
    }

    private void setupChart() {
        try {
            candleChart.getDescription().setEnabled(false);
            candleChart.setDrawGridBackground(false);
            candleChart.getLegend().setEnabled(false);
            
            // 啟用雙指縮放
            candleChart.setPinchZoom(true);
            candleChart.setDoubleTapToZoomEnabled(true);

            XAxis xAxis = candleChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(Color.parseColor("#A0A0A0")); 
            
            xAxis.setValueFormatter(new ValueFormatter() {
                private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    int index = (int) value;
                    if (currentTimestamps != null && index >= 0 && index < currentTimestamps.size()) {
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                        Long ts = currentTimestamps.get(index);
                        if (ts != null) {
                            return sdf.format(new Date(ts * 1000));
                        }
                    }
                    return "";
                }
            });

            candleChart.getAxisRight().setEnabled(false);
            YAxis leftAxis = candleChart.getAxisLeft();
            leftAxis.setDrawGridLines(true);
            leftAxis.setGridColor(Color.parseColor("#262626")); 
            leftAxis.setDrawAxisLine(false);
            leftAxis.setTextColor(Color.parseColor("#A0A0A0")); 

            // 監聽十字指針選取事件 (連動更新座標數據)
            candleChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    try {
                        int index = (int) e.getX();
                        if (currentCloses != null && index >= 0 && index < currentCloses.size()) {
                            Double selectedPrice = currentCloses.get(index);
                            
                            if (selectedPrice != null) {
                                // 僅連動座標顯示，不更動左側最新價格顯示 (使用 Spannable 動態渲染顏色)
                                updateCoordinatesText(selectedPrice);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "onValueSelected 運算錯誤: ", ex);
                    }
                }

                @Override
                public void onNothingSelected() {
                    // 當點選空白處指針消失時，強制再將指針彈回最右側，維持指針永遠顯示 (動態搜尋 Highlight DataSet 解決失效問題)
                    try {
                        if (currentCloses != null && !currentCloses.isEmpty()) {
                            int lastIndex = currentCloses.size() - 1;
                            highlightLatestEntry(lastIndex);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "onNothingSelected 處理錯誤: ", ex);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "setupChart 配置錯誤: ", e);
        }
    }

    /**
     * 更新十字游標座標 TextView 的值 (僅顯示價格，且價格會動態標記紅綠色，時間與漲跌幅資訊已移除)
     */
    private void updateCoordinatesText(Double selectedPrice) {
        try {
            if (selectedPrice == null) {
                tvStatsCoordinates.setText(" --");
                return;
            }

            // 🎯 使用全局當日昨收價格 (todayPreviousClose) 作為漲跌判斷基準
            double percent = todayPreviousClose > 0 ? ((selectedPrice - todayPreviousClose) / todayPreviousClose) * 100 : 0.0;
            String priceStr = String.format("%.2f", selectedPrice);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(priceStr);

            // 決定顏色 (漲紅跌綠)
            int color = (percent >= 0) ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
            builder.setSpan(new ForegroundColorSpan(color), 0, priceStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvStatsCoordinates.setText(builder);
        } catch (Exception e) {
            Log.e(TAG, "updateCoordinatesText 錯誤: ", e);
        }
    }

    /**
     * 重置或初始化數據顯示為最新收盤價與漲幅
     */
    private void resetStatsToLatest() {
        try {
            if (currentCloses != null && !currentCloses.isEmpty() && currentTimestamps != null && !currentTimestamps.isEmpty()) {
                int lastIndex = currentCloses.size() - 1;
                Double latestPriceVal = currentCloses.get(lastIndex);

                if (latestPriceVal != null) {
                    double latestPrice = latestPriceVal;
                    
                    // 🎯 永遠使用今日實際的前一日收盤價 (todayPreviousClose) 計算市價漲跌幅，不隨時間標籤切換而失真！
                    double percent = todayPreviousClose > 0 ? ((latestPrice - todayPreviousClose) / todayPreviousClose) * 100 : 0.0;
                    
                    // 左側現價區：固定顯示最新市價與漲跌幅
                    tvStatsPricePercent.setText(String.format("%.2f (%.2f%%)", latestPrice, percent));
                    tvStatsPricePercent.setTextColor(percent >= 0 ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
                    
                    // 右側座標區：顯示最新一根線的座標 (使用 Spannable)
                    updateCoordinatesText(latestPrice);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "resetStatsToLatest 錯誤: ", e);
        }
    }

    private void loadStockData(String symbol, String interval, String range) {
        tvStatsPricePercent.setText("載入中...");
        Log.d(TAG, "loadStockData: 開始拉取 " + symbol + " 資料, 區間: " + interval + ", 範圍: " + range);

        stockService.fetchChartData(symbol, interval, range, new com.example.javatoandroid.service.StockServiceCallback() {
            @Override
            public void onSuccess(StockData data) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    try {
                        // 取消將股票名稱寫回搜尋欄，只保留代碼
                        String rawSymbol = symbol.replace(".TW", "").replace(".TWO", "");
                        if (getActivity() != null) {
                            EditText etMainSearch = getActivity().findViewById(R.id.etStockSymbol);
                            if (etMainSearch != null) {
                                etMainSearch.setText(rawSymbol);
                                etMainSearch.setSelection(rawSymbol.length());
                            }
                        }

                        double price = data.currentPrice;
                        etPrice.setText(String.format("%.2f", price));

                        // 提早計算並設定即時漲跌幅
                        double basePrice = data.previousClose > 0.0 ? data.previousClose : price;
                        double change = price - basePrice;
                        double percent = basePrice > 0 ? (change / basePrice) * 100 : 0.0;
                        String sign = change > 0 ? "+" : "";
                        String colorStr = change > 0 ? "#F44336" : (change < 0 ? "#4CAF50" : "#E0E0E0");
                        String formattedText = String.format("<font color='%s'>%.2f (%s%.2f%%)</font> <font color='#888888'><small>[延遲20分]</small></font>", colorStr, price, sign, percent);
                        tvStatsPricePercent.setText(android.text.Html.fromHtml(formattedText, android.text.Html.FROM_HTML_MODE_COMPACT));
                        
                        // 圖表陣列防護
                        if (data.isChartDataMissing) {
                            Log.w(TAG, "onSuccess: 圖表陣列資料缺失 (可能剛開盤或無交易資料)");
                            candleChart.clear();
                            candleChart.setNoDataText("暫無圖表資料，但即時報價已更新");
                            candleChart.invalidate();
                            return;
                        }

                        currentTimestamps = data.timestamps;
                        List<Double> opens = data.opens;
                        List<Double> highs = data.highs;
                        List<Double> lows = data.lows;
                        currentCloses = data.closes;

                        // 🎯 設定基準價
                        firstPriceBaseline = data.previousClose;
                        if (firstPriceBaseline <= 0.0 && opens != null) {
                            for (Double openVal : opens) {
                                if (openVal != null) {
                                    firstPriceBaseline = openVal;
                                    break;
                                }
                            }
                        }

                        // 更新全局當日昨收價格
                        if (data.previousClose > 0.0) {
                            todayPreviousClose = data.previousClose;
                        } else if ("1m".equals(interval)) {
                            todayPreviousClose = firstPriceBaseline;
                        } else if (todayPreviousClose <= 0.0 && currentCloses != null && currentCloses.size() >= 2) {
                            Double secondToLast = currentCloses.get(currentCloses.size() - 2);
                            if (secondToLast != null) {
                                todayPreviousClose = secondToLast;
                            }
                        }

                        // 1. 根據不同週期，封裝不同的圖表資料
                        CombinedData combinedData = new CombinedData();
                        YAxis leftAxis = candleChart.getAxisLeft();
                        leftAxis.removeAllLimitLines(); // 先清空所有舊的標記線

                        if ("1m".equals(interval)) {
                            List<Entry> priceEntries = new ArrayList<>();
                            List<Integer> lineColors = new ArrayList<>();
                            
                            float highestPrice = Float.MIN_VALUE;
                            float lowestPrice = Float.MAX_VALUE;

                            for (int i = 0; i < currentCloses.size(); i++) {
                                Double c = currentCloses.get(i);
                                if (c != null) {
                                    priceEntries.add(new Entry(i, c.floatValue()));
                                    Double targetPrice = c;
                                    if (i + 1 < currentCloses.size() && currentCloses.get(i + 1) != null) {
                                        targetPrice = currentCloses.get(i + 1);
                                    }
                                    if (targetPrice >= firstPriceBaseline) {
                                        lineColors.add(Color.parseColor("#F44336"));
                                    } else {
                                        lineColors.add(Color.parseColor("#4CAF50"));
                                    }
                                    float val = c.floatValue();
                                    if (val > highestPrice) highestPrice = val;
                                    if (val < lowestPrice) lowestPrice = val;
                                }
                            }

                            if (priceEntries.isEmpty()) {
                                candleChart.clear();
                                return;
                            }

                            LineDataSet priceDataSet = new LineDataSet(priceEntries, "走勢");
                            priceDataSet.setLineWidth(1.8f);
                            priceDataSet.setDrawCircles(false);
                            priceDataSet.setDrawValues(false);
                            priceDataSet.setHighlightEnabled(true);
                            priceDataSet.setHighLightColor(Color.parseColor("#88FFFFFF"));
                            priceDataSet.setHighlightLineWidth(1.2f);
                            priceDataSet.setDrawHorizontalHighlightIndicator(true);
                            priceDataSet.setDrawVerticalHighlightIndicator(true);
                            priceDataSet.setColors(lineColors);
                            priceDataSet.setDrawFilled(false);
                            
                            combinedData.setData(new LineData(priceDataSet));

                            if (highestPrice != Float.MIN_VALUE) {
                                LimitLine maxLine = new LimitLine(highestPrice, String.format("最高 %.2f", highestPrice));
                                maxLine.setLineColor(Color.parseColor("#E91E63"));
                                maxLine.setLineWidth(0.8f);
                                maxLine.enableDashedLine(10f, 10f, 0f);
                                maxLine.setTextColor(Color.parseColor("#E91E63"));
                                maxLine.setTextSize(12f);
                                leftAxis.addLimitLine(maxLine);
                            }

                            if (lowestPrice != Float.MAX_VALUE) {
                                LimitLine minLine = new LimitLine(lowestPrice, String.format("最低 %.2f", lowestPrice));
                                minLine.setLineColor(Color.parseColor("#00BCD4"));
                                minLine.setLineWidth(0.8f);
                                minLine.enableDashedLine(10f, 10f, 0f);
                                minLine.setTextColor(Color.parseColor("#00BCD4"));
                                minLine.setTextSize(12f);
                                leftAxis.addLimitLine(minLine);
                            }

                            if (firstPriceBaseline > 0) {
                                LimitLine baseLine = new LimitLine((float) firstPriceBaseline, String.format("昨收 %.2f", firstPriceBaseline));
                                baseLine.setLineColor(Color.parseColor("#888888"));
                                baseLine.setLineWidth(0.8f);
                                baseLine.enableDashedLine(10f, 10f, 0f);
                                baseLine.setTextColor(Color.parseColor("#888888"));
                                baseLine.setTextSize(12f);
                                leftAxis.addLimitLine(baseLine);
                            }

                        } else {
                            List<CandleEntry> candleEntries = new ArrayList<>();
                            float highestPrice = Float.MIN_VALUE;
                            float lowestPrice = Float.MAX_VALUE;

                            for (int i = 0; i < currentTimestamps.size(); i++) {
                                if (i < opens.size() && i < highs.size() && i < lows.size() && i < currentCloses.size() &&
                                    opens.get(i) != null && highs.get(i) != null && lows.get(i) != null && currentCloses.get(i) != null) {
                                    
                                    float o = opens.get(i).floatValue();
                                    float h = highs.get(i).floatValue();
                                    float l = lows.get(i).floatValue();
                                    float c = currentCloses.get(i).floatValue();
                                    
                                    candleEntries.add(new CandleEntry(i, h, l, o, c));
                                    if (h > highestPrice) highestPrice = h;
                                    if (l < lowestPrice) lowestPrice = l;
                                }
                            }

                            if (candleEntries.isEmpty()) {
                                candleChart.clear();
                                return;
                            }

                            CandleDataSet candleDataSet = new CandleDataSet(candleEntries, "K線");
                            candleDataSet.setShadowColorSameAsCandle(true);
                            candleDataSet.setShadowWidth(0.8f);
                            candleDataSet.setDecreasingColor(Color.parseColor("#4CAF50"));
                            candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
                            candleDataSet.setIncreasingColor(Color.parseColor("#F44336"));
                            candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
                            candleDataSet.setNeutralColor(Color.parseColor("#E0E0E0"));
                            candleDataSet.setDrawValues(false);
                            candleDataSet.setHighlightEnabled(true);
                            candleDataSet.setHighLightColor(Color.parseColor("#88FFFFFF"));
                            candleDataSet.setHighlightLineWidth(1.2f);
                            candleDataSet.setDrawHorizontalHighlightIndicator(true);
                            candleDataSet.setDrawVerticalHighlightIndicator(true);
                            
                            combinedData.setData(new CandleData(candleDataSet));

                            if (highestPrice != Float.MIN_VALUE) {
                                LimitLine maxLine = new LimitLine(highestPrice, String.format("最高 %.2f", highestPrice));
                                maxLine.setLineColor(Color.parseColor("#E91E63")); 
                                maxLine.setLineWidth(0.8f);
                                maxLine.enableDashedLine(10f, 10f, 0f);
                                maxLine.setTextColor(Color.parseColor("#E91E63"));
                                maxLine.setTextSize(12f);
                                leftAxis.addLimitLine(maxLine);
                            }
                            if (lowestPrice != Float.MAX_VALUE) {
                                LimitLine minLine = new LimitLine(lowestPrice, String.format("最低 %.2f", lowestPrice));
                                minLine.setLineColor(Color.parseColor("#00BCD4")); 
                                minLine.setLineWidth(0.8f);
                                minLine.enableDashedLine(10f, 10f, 0f);
                                minLine.setTextColor(Color.parseColor("#00BCD4"));
                                minLine.setTextSize(12f);
                                leftAxis.addLimitLine(minLine);
                            }

                            List<Entry> ma5Entries = new ArrayList<>();
                            List<Entry> ma20Entries = new ArrayList<>();
                            for (int i = 0; i < currentCloses.size(); i++) {
                                if (i >= 4) {
                                    double sum = 0;
                                    int validCount = 0;
                                    for (int j = i - 4; j <= i; j++) {
                                        if (j < currentCloses.size() && currentCloses.get(j) != null) {
                                            sum += currentCloses.get(j);
                                            validCount++;
                                        }
                                    }
                                    if (validCount > 0) ma5Entries.add(new Entry(i, (float) (sum / validCount)));
                                }
                                if (i >= 19) {
                                    double sum = 0;
                                    int validCount = 0;
                                    for (int j = i - 19; j <= i; j++) {
                                        if (j < currentCloses.size() && currentCloses.get(j) != null) {
                                            sum += currentCloses.get(j);
                                            validCount++;
                                        }
                                    }
                                    if (validCount > 0) ma20Entries.add(new Entry(i, (float) (sum / validCount)));
                                }
                            }

                            List<ILineDataSet> lineDataSets = new ArrayList<>();
                            if (!ma5Entries.isEmpty()) {
                                LineDataSet ma5Set = new LineDataSet(ma5Entries, "MA5");
                                ma5Set.setColor(Color.parseColor("#2196F3")); 
                                ma5Set.setLineWidth(1.2f);
                                ma5Set.setDrawCircles(false);
                                ma5Set.setDrawValues(false);
                                ma5Set.setHighlightEnabled(false);
                                lineDataSets.add(ma5Set);
                            }
                            if (!ma20Entries.isEmpty()) {
                                LineDataSet ma20Set = new LineDataSet(ma20Entries, "MA20");
                                ma20Set.setColor(Color.parseColor("#FFC107")); 
                                ma20Set.setLineWidth(1.2f);
                                ma20Set.setDrawCircles(false);
                                ma20Set.setDrawValues(false);
                                ma20Set.setHighlightEnabled(false);
                                lineDataSets.add(ma20Set);
                            }
                            
                            if (!lineDataSets.isEmpty()) {
                                LineData lineData = new LineData(lineDataSets);
                                combinedData.setData(lineData);
                            }
                        }

                        // 4. 設定 X 軸格式
                        XAxis xAxis = candleChart.getXAxis();
                        xAxis.setDrawLabels("1m".equals(interval));

                        // 6. 裝填至圖表並刷新
                        candleChart.setData(combinedData);
                        candleChart.notifyDataSetChanged(); // 通知數據已更新，確保 CombinedChart renderer 計算最新的 DataSet 範圍與 highlight
                        candleChart.invalidate();

                        // 初始化現價與座標列文字
                        resetStatsToLatest();

                        // 🎯 十字指針預設指向最右側最後一根線/K線 (利用 post 避開渲染期被擦除的 Bug)
                        final int lastIndex = currentCloses.size() - 1;
                        candleChart.post(() -> {
                            try {
                                if (isAdded() && candleChart != null && lastIndex >= 0) {
                                    highlightLatestEntry(lastIndex);
                                    Log.d(TAG, "post-run: 預設高亮十字指針成功, 指向索引 " + lastIndex);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "post-run highlight 錯誤: ", ex);
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "資料解析或渲染失敗: ", e);
                        tvStatsPricePercent.setText("解析異常");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    Log.e(TAG, "onFailure 資料獲取失敗: " + errorMessage);
                    tvStatsPricePercent.setText("失敗: " + errorMessage);
                });
            }
        });
    }

      /**
       * 支援多資料集 (CombinedChart) 的精確指針定位與高亮
       */
      private void highlightLatestEntry(int lastIndex) {
          try {
              if (lastIndex < 0 || currentCloses == null || currentCloses.isEmpty()) return;
              
              com.github.mikephil.charting.data.CombinedData data = candleChart.getData();
              if (data == null) return;
              
              com.github.mikephil.charting.highlight.Highlight highlight = null;
              List<com.github.mikephil.charting.data.BarLineScatterCandleBubbleData> allData = data.getAllData();
              if (allData != null) {
                  for (int dataIdx = 0; dataIdx < allData.size(); dataIdx++) {
                      com.github.mikephil.charting.data.BarLineScatterCandleBubbleData subData = allData.get(dataIdx);
                      if (subData == null) continue;
                      int dataSetCount = subData.getDataSetCount();
                      for (int setIdx = 0; setIdx < dataSetCount; setIdx++) {
                          com.github.mikephil.charting.interfaces.datasets.IDataSet set = subData.getDataSetByIndex(setIdx);
                          if (set != null && set.isHighlightEnabled()) {
                              float yVal = 0f;
                              if (set.getEntryCount() > lastIndex) {
                                  yVal = set.getEntryForIndex(lastIndex).getY();
                              }
                              com.github.mikephil.charting.highlight.Highlight hl = new com.github.mikephil.charting.highlight.Highlight((float) lastIndex, yVal, setIdx);
                              hl.setDataIndex(dataIdx); // 設定 sub-data 類型索引
                              highlight = hl;
                              break;
                          }
                      }
                      if (highlight != null) break;
                  }
              }
              
              if (highlight != null) {
                  candleChart.highlightValue(highlight);
              }
          } catch (Exception e) {
              Log.e(TAG, "highlightLatestEntry 錯誤: ", e);
          }
      }
  }
