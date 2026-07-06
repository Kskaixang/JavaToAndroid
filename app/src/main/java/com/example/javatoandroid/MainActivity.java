package com.example.javatoandroid;

import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 重構後的主畫面 (Activity)。
 * 現在它不再身兼多職 (不再親自處理圖表與 API)，而是成為一個純粹的「外殼」。
 * 它的唯一工作是：提供上方搜尋列，並將使用者的指令傳遞給裝載於內部的 Fragment。
 * 這樣設計能大幅降低維護難度！
 */
public class MainActivity extends AppCompatActivity {

    private EditText etStockSymbol;
    private Button btnSearch;
    private StockChartFragment stockChartFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 綁定主畫面佈局 (內含搜尋列 + Fragment 空白容器)
        setContentView(R.layout.activity_main);

        // 綁定上方搜尋列的輸入框與按鈕
        etStockSymbol = findViewById(R.id.etStockSymbol);
        btnSearch = findViewById(R.id.btnSearch);

        // 步驟 1：建立並裝載 StockChartFragment 
        // 這就是組件化 (Component-based) 的精髓
        if (savedInstanceState == null) {
            stockChartFragment = new StockChartFragment();
            // 透過 FragmentManager，把我們寫好的 K 線圖 Fragment 塞進畫面的容器中
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, stockChartFragment)
                    .commit();
        } else {
            // 如果手機翻轉螢幕導致重建，就從系統找回已經存在的 Fragment
            stockChartFragment = (StockChartFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        }

        // 步驟 2：監聽搜尋按鈕的點擊
        btnSearch.setOnClickListener(v -> {
            // 取得使用者輸入的字串，去除前後空白
            String symbol = etStockSymbol.getText().toString().trim();
            if (!symbol.isEmpty() && stockChartFragment != null) {
                // 【核心串接】把代號丟給子組件 Fragment，讓它自己去跑網路請求與畫圖
                stockChartFragment.setStockSymbol(symbol);
                
                // (貼心小功能) 按下搜尋後，立刻自動隱藏手機的虛擬小鍵盤
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });
    }
}