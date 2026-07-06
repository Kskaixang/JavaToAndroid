package com.example.javatoandroid;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.javatoandroid.model.service.ApiClient;
import com.example.javatoandroid.model.service.YahooApiService;
import com.example.javatoandroid.model.yahoo.YahooChartResponse;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockChartFragment extends Fragment {

    // 頂部 UI
    private TextView tvCurrentPrice;
    private TextView tab1D, tabDay, tabWeek, tabMonth;
    private CandleStickChart candleChart;

    // 下單面板 UI
    private TextView tvUnrealizedPnl, tvInventory, tvAccountBalance, tvEstBalance, tvEstInventory;
    private TextView tabBuy, tabSell, tabLimit, tabMarket;
    private Button btnOrderReport, btnSubmitOrder;
    private EditText etPrice, etQuantity;
    private Button btnMinusPrice, btnPlusPrice, btnMinusQuantity, btnPlusQuantity, btnPlus10Quantity, btnFetchPrice;

    private YahooApiService yahooApiService;
    private String currentSymbol = "2330.TW"; // 加上 .TW 確保初次載入 API 正常
    private String currentInterval = "1m";

    // 【模擬後端資料】 等您 Oracle 寫好後，這些將被 BackendApiService 取代
    private double mockBalance = 1000000.0;
    private int mockInventory = 5;
    private boolean isBalanceHidden = true; // 預設霧化餘額
    
    // 紀錄目前的狀態 (買進或賣出)
    private boolean isBuyAction = true;

    private List<Long> currentTimestamps = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_chart, container, false);

        // 綁定圖表與標籤
        tvCurrentPrice = view.findViewById(R.id.tvCurrentPrice);
        candleChart = view.findViewById(R.id.candleChart);
        tab1D = view.findViewById(R.id.tab1D);
        tabDay = view.findViewById(R.id.tabDay);
        tabWeek = view.findViewById(R.id.tabWeek);
        tabMonth = view.findViewById(R.id.tabMonth);

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

        yahooApiService = ApiClient.getYahooClient().create(YahooApiService.class);

        setupChart();
        setupListeners();
        setupOrderPanelListeners();
        
        // 更新初始的帳戶餘額與庫存顯示
        updateBalanceDisplay();
        updateEstimates();

        loadStockData(currentSymbol, "1m", "1d");

        return view;
    }

    public void setStockSymbol(String symbol) {
        // 從 "2330 台積電" 中提取純代碼 "2330"
        String rawSymbol = symbol.split(" ")[0];

        // 確保帶有市場後綴供 API 抓取
        if (!rawSymbol.toUpperCase().endsWith(".TW") && !rawSymbol.toUpperCase().endsWith(".TWO")) {
            this.currentSymbol = rawSymbol + ".TW";
        } else {
            this.currentSymbol = rawSymbol.toUpperCase();
        }
        
        // 模擬：更換股票時，隨機產生該股目前的庫存量
        mockInventory = (int)(Math.random() * 10);
        updateBalanceDisplay();
        updateEstimates(); // 重新試算
        
        // 使用當前的時間區間去抓新股票
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
    }

    private void switchTab(TextView selectedTab, String interval, String range) {
        // 將所有的標籤復原為非選取狀態 (夜間模式字體色)
        TextView[] tabs = {tab1D, tabDay, tabWeek, tabMonth};
        for (TextView tab : tabs) {
            tab.setBackgroundColor(Color.TRANSPARENT);
            tab.setTextColor(Color.parseColor("#A0A0A0"));
        }
        // 反白選中的標籤
        selectedTab.setBackgroundColor(Color.parseColor("#2196F3"));
        selectedTab.setTextColor(Color.WHITE);
        
        currentInterval = interval;
        loadStockData(currentSymbol, interval, range);
    }

    private void setupOrderPanelListeners() {
        // 餘額與庫存點擊時，連動切換霧化狀態
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

        // 買進 / 賣出 平扁方塊按鈕切換
        tabBuy.setOnClickListener(v -> {
            isBuyAction = true;
            tabBuy.setBackgroundColor(Color.parseColor("#F44336"));
            tabBuy.setTextColor(Color.WHITE);
            tabSell.setBackgroundColor(Color.TRANSPARENT);
            tabSell.setTextColor(Color.parseColor("#A0A0A0"));
            // 連動下方下單鈕顏色
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

        // 限價 / 市價 切換
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

        // 加減與取價按鈕
        btnMinusPrice.setOnClickListener(v -> adjustValue(etPrice, -0.5, true));
        btnPlusPrice.setOnClickListener(v -> adjustValue(etPrice, 0.5, true));
        btnMinusQuantity.setOnClickListener(v -> adjustValue(etQuantity, -1.0, false));
        btnPlusQuantity.setOnClickListener(v -> adjustValue(etQuantity, 1.0, false));
        btnPlus10Quantity.setOnClickListener(v -> adjustValue(etQuantity, 10.0, false));
        btnFetchPrice.setOnClickListener(v -> {
            try {
                etPrice.setText(tvCurrentPrice.getText().toString());
            } catch (Exception e) {}
        });

        // 監聽價格或數量的輸入變化，即時觸發下方的「試算」
        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateEstimates(); }
        };
        etPrice.addTextChangedListener(tw);
        etQuantity.addTextChangedListener(tw);

        // 送出委託
        btnSubmitOrder.setOnClickListener(v -> {
            Toast.makeText(getContext(), "已送出委託！等後端就緒即可發 API", Toast.LENGTH_SHORT).show();
            btnOrderReport.setText("前筆委託: 等待中");
        });
    }

    private void adjustValue(EditText editText, double amount, boolean isDecimal) {
        try {
            double currentVal = Double.parseDouble(editText.getText().toString());
            double newVal = currentVal + amount;
            if (newVal < 0) newVal = 0;
            if (isDecimal) {
                editText.setText(String.format("%.2f", newVal));
            } else {
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
            // 這裡未實現損益暫時用 0 模擬，後續可替換為真實計算結果
            tvUnrealizedPnl.setText("$0");
            tvAccountBalance.setText(String.format("$%,.0f", mockBalance));
            tvInventory.setText(String.valueOf(mockInventory));
        }
    }

    // 【進階功能：交易試算核心邏輯】
    private void updateEstimates() {
        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            int qty = Integer.parseInt(etQuantity.getText().toString());
            
            // 由於目前不確定您遊戲內的設定是 1張=1000股 還是 1單位=1元
            // 這裡先採用 1 單位就是畫面上價格的 1000 倍 (台股標準)，或者直接相乘
            double totalCost = price * qty * 1000; 
            
            double estBalance;
            int estInventory;
            if (isBuyAction) {
                estBalance = mockBalance - totalCost; // 買進扣錢
                estInventory = mockInventory + qty;   // 買進增加庫存
            } else {
                estBalance = mockBalance + totalCost; // 賣出得錢
                estInventory = mockInventory - qty;   // 賣出扣庫存
            }
            
            if (isBalanceHidden) {
                tvEstBalance.setText("*****");
                tvEstInventory.setText("*****");
            } else {
                tvEstBalance.setText(String.format("$%,.0f", estBalance));
                tvEstInventory.setText(String.valueOf(estInventory));
            }
            
            // 防呆檢測：若餘額不足或賣出超過庫存，顯示紅色警告
            if (estBalance < 0 || estInventory < 0) {
                tvEstBalance.setTextColor(Color.parseColor("#F44336")); 
                tvEstInventory.setTextColor(Color.parseColor("#F44336"));
            } else {
                tvEstBalance.setTextColor(Color.parseColor("#03A9F4")); 
                tvEstInventory.setTextColor(Color.parseColor("#03A9F4"));
            }

        } catch (Exception e) {
            tvEstBalance.setText("--");
            tvEstInventory.setText("--");
        }
    }

    private void setupChart() {
        candleChart.getDescription().setEnabled(false);
        candleChart.setDrawGridBackground(false);
        // 【優化】完全隱藏圖例 (左下角不再出現 2330.TW 的小方塊)
        candleChart.getLegend().setEnabled(false);
        
        XAxis xAxis = candleChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#A0A0A0")); // 夜間模式文字
        
        xAxis.setValueFormatter(new ValueFormatter() {
            private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < currentTimestamps.size()) {
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                    return sdf.format(new Date(currentTimestamps.get(index) * 1000));
                }
                return "";
            }
        });

        candleChart.getAxisRight().setEnabled(false);
        YAxis leftAxis = candleChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        // 夜間模式暗色網格線，不刺眼
        leftAxis.setGridColor(Color.parseColor("#333333")); 
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.parseColor("#A0A0A0")); 
    }

    private void loadStockData(String symbol, String interval, String range) {
        tvCurrentPrice.setText("載入中...");

        Call<YahooChartResponse> call = yahooApiService.getChart(symbol, interval, range);
        call.enqueue(new Callback<YahooChartResponse>() {
            @Override
            public void onResponse(Call<YahooChartResponse> call, Response<YahooChartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().chart.result != null) {
                    try {
                        YahooChartResponse.Result result = response.body().chart.result.get(0);

                        // 【重點優化】：解析 shortName，動態替換頂部搜尋框的標題
                        String rawSymbol = symbol.replace(".TW", "").replace(".TWO", "");
                        String chineseName = getChineseName(rawSymbol);
                        String displayName = rawSymbol + " " + (chineseName != null ? chineseName : (result.meta.shortName != null ? result.meta.shortName : ""));
                        
                        // 將 "2330 台積電" 推送回 MainActivity 的 EditText 中
                        if (getActivity() != null) {
                            EditText etMainSearch = getActivity().findViewById(R.id.etStockSymbol);
                            if (etMainSearch != null) {
                                // 若使用者已經自己輸入了中文，就不強制蓋掉，除非原本只打代碼
                                String currentSearchText = etMainSearch.getText().toString();
                                if (!currentSearchText.contains(" ") || currentSearchText.equals("2330.TW")) {
                                    etMainSearch.setText(displayName);
                                    etMainSearch.setSelection(displayName.length());
                                }
                            }
                        }

                        double price = result.meta.regularMarketPrice;
                        tvCurrentPrice.setText(String.format("%.2f", price));
                        // 將最新市價填入下單區
                        etPrice.setText(String.format("%.2f", price));

                        currentTimestamps = result.timestamp;
                        List<Double> opens = result.indicators.quote.get(0).open;
                        List<Double> highs = result.indicators.quote.get(0).high;
                        List<Double> lows = result.indicators.quote.get(0).low;
                        List<Double> closes = result.indicators.quote.get(0).close;

                        List<CandleEntry> entries = new ArrayList<>();
                        if (currentTimestamps != null) {
                            for (int i = 0; i < currentTimestamps.size(); i++) {
                                if (opens.get(i) != null && highs.get(i) != null && lows.get(i) != null && closes.get(i) != null) {
                                    float o = opens.get(i).floatValue();
                                    float h = highs.get(i).floatValue();
                                    float l = lows.get(i).floatValue();
                                    float c = closes.get(i).floatValue();
                                    entries.add(new CandleEntry(i, h, l, o, c));
                                }
                            }
                        }

                        if (entries.isEmpty()) {
                            candleChart.clear();
                        } else {
                            XAxis xAxis = candleChart.getXAxis();
                            if ("1m".equals(interval)) {
                                xAxis.setDrawLabels(true);
                            } else {
                                xAxis.setDrawLabels(false);
                            }

                            // 【優化】圖例名稱改為「走勢」
                            CandleDataSet dataSet = new CandleDataSet(entries, "走勢");
                            dataSet.setShadowColorSameAsCandle(true);
                            dataSet.setShadowWidth(1.0f);
                            dataSet.setDecreasingColor(Color.parseColor("#4CAF50")); 
                            dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
                            dataSet.setIncreasingColor(Color.parseColor("#F44336")); 
                            dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
                            dataSet.setNeutralColor(Color.parseColor("#E0E0E0"));
                            dataSet.setDrawValues(false);

                            CandleData candleData = new CandleData(dataSet);
                            candleChart.setData(candleData);
                            candleChart.invalidate();
                        }

                    } catch (Exception e) {
                        tvCurrentPrice.setText("解析異常");
                    }
                } else {
                    tvCurrentPrice.setText("查無代碼");
                }
            }

            @Override
            public void onFailure(Call<YahooChartResponse> call, Throwable t) {
                tvCurrentPrice.setTextSize(14f);
                tvCurrentPrice.setText("失敗: " + t.getMessage());
            }
        });
    }

    // 模擬：股票代碼轉中文名稱對照表 (由於 Yahoo API 的 shortName 通常是英文)
    private String getChineseName(String symbol) {
        switch (symbol) {
            case "2330": return "台積電";
            case "2303": return "聯電";
            case "2454": return "聯發科";
            case "2317": return "鴻海";
            case "2603": return "長榮";
            case "2881": return "富邦金";
            case "2882": return "國泰金";
            case "2891": return "中信金";
            default: return null;
        }
    }
}
