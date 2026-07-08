package com.example.javatoandroid.ui.activity;

import com.example.javatoandroid.R;
import com.example.javatoandroid.ui.fragment.StockChartFragment;
import com.example.javatoandroid.utils.NetworkMonitor;
import com.example.javatoandroid.ui.dialog.NetworkWarningDialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 主畫面 Activity (外殼控制項)
 * - 負責頂部導覽列：包含 Google 登入模擬狀態與圓形頭像按鈕
 * - 整合現代化嵌入式搜尋欄 (放大鏡與輸入框)
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivUserAvatar;
    private TextView btnSearch;
    private EditText etStockSymbol;
    private StockChartFragment stockChartFragment;

    // 用於本機儲存使用者登入狀態與 Token
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GOOGLE_TOKEN = "googleToken";

    // 網路狀態監聽器相關組件 (已抽取為獨立類別)
    private NetworkMonitor networkMonitor;
    private NetworkWarningDialog networkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 綁定 UI 元件
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        btnSearch = findViewById(R.id.btnSearch);
        etStockSymbol = findViewById(R.id.etStockSymbol);

        // 步驟 1：載入並裝載 K 線圖 Fragment
        if (savedInstanceState == null) {
            stockChartFragment = new StockChartFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, stockChartFragment)
                    .commit();
        } else {
            stockChartFragment = (StockChartFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        }

        // 步驟 2：初始化/還原登入狀態 (檢查 Token)
        checkAndSyncLoginState();

        // 步驟 3：設定搜尋事件 (支援點選 🔍 或鍵盤右下角的「搜尋」鍵)
        btnSearch.setOnClickListener(v -> performSearch());
        etStockSymbol.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // 設定清空搜尋欄按鈕
        ImageView btnClearSearch = findViewById(R.id.btnClearSearch);
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etStockSymbol.setText("");
                etStockSymbol.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etStockSymbol, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        // 步驟 4：設定 Google 頭像按鈕點擊監聽
        ivUserAvatar.setOnClickListener(v -> {
            boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
            if (isLoggedIn) {
                // 已登入：跳出模擬的漢堡選單 (常規功能選單)
                showUserMenu();
            } else {
                // 未登入：啟動模擬的 Google 授權登入流程
                triggerGoogleLogin();
            }
        });

        // 步驟 5：啟動網路狀態監聽器
        setupNetworkListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 釋放網路監聽器，避免 Memory Leak
        if (networkMonitor != null) {
            networkMonitor.stopListening();
        }
    }

    /**
     * 設定網路連線狀態的監聽器
     * 使用已抽取的 NetworkMonitor 組件，保持 MainActivity 的職責單一 (乾淨)
     */
    private void setupNetworkListener() {
        networkDialog = new NetworkWarningDialog(this);
        
        networkMonitor = new NetworkMonitor(this, new NetworkMonitor.NetworkStateListener() {
            @Override
            public void onNetworkAvailable() {
                if (networkDialog != null) {
                    networkDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, "網路已恢復連線！", Toast.LENGTH_SHORT).show();
                
                // 網路恢復後，如果有輸入代號，自動幫使用者重新刷新資料
                if (etStockSymbol != null && !etStockSymbol.getText().toString().trim().isEmpty()) {
                    performSearch();
                }
            }

            @Override
            public void onNetworkLost() {
                networkDialog.show();
            }
        });

        // 開始監聽
        networkMonitor.startListening();
    }

    /**
     * 執行股票代號搜尋
     */
    private void performSearch() {
        String symbol = etStockSymbol.getText().toString().trim();
        if (!symbol.isEmpty() && stockChartFragment != null) {
            // 切割出純代碼 (例如 "2330 台積電" ➔ "2330") 傳遞給圖表子組件
            stockChartFragment.setStockSymbol(symbol);

            // 收起虛擬鍵盤
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } else {
            Toast.makeText(this, "請輸入股票代號", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 檢查並同步使用者的 Google 登入頭像狀態
     */
    private void checkAndSyncLoginState() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            // 已登入：將頭像改為綠色的已驗證打勾小人 (代表 Google 登入狀態)
            ivUserAvatar.setImageResource(android.R.drawable.presence_online);
            ivUserAvatar.setBackgroundColor(Color.parseColor("#4CAF50")); // 綠底
        } else {
            // 未登入：維持預設淡灰色位置圖標
            ivUserAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            ivUserAvatar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * 觸發模擬的 Google 登入程序
     */
    private void triggerGoogleLogin() {
        Toast.makeText(this, "正在轉跳 Google 授權登入...", Toast.LENGTH_SHORT).show();
        
        // 延時模擬登入成功 (存入 Token，維持登入狀態)
        ivUserAvatar.postDelayed(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_GOOGLE_TOKEN, "mock-google-oauth2-token-20260706");
            editor.apply();

            checkAndSyncLoginState();
            Toast.makeText(MainActivity.this, "Google 登入成功！(Token 已存入本機免登)", Toast.LENGTH_LONG).show();
        }, 1200);
    }

    /**
     * 顯示已登入使用者的漢堡選單 (PopupWindow)
     */
    private void showUserMenu() {
        // 動態建立一個簡單的選單佈局
        LinearLayout menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.parseColor("#262626"));
        menuLayout.setPadding(24, 24, 24, 24);

        // 準備選單功能項目
        String[] menuItems = {"個人帳戶資訊", "委託歷史紀錄", "安全與隱私設定", "登出帳戶"};
        for (String itemText : menuItems) {
            TextView tv = new TextView(this);
            tv.setText(itemText);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(14f);
            tv.setPadding(24, 18, 24, 18);
            tv.setClickable(true);
            tv.setFocusable(true);
            
            // 設定點擊背景高亮
            tv.setBackgroundResource(android.R.drawable.list_selector_background);

            // 為個別項目綁定點擊事件
            if ("登出帳戶".equals(itemText)) {
                tv.setOnClickListener(v -> {
                    logoutUser();
                });
            } else {
                tv.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "點擊了: " + itemText, Toast.LENGTH_SHORT).show();
                });
            }
            menuLayout.addView(tv);
        }

        // 實例化寬度為 180dp 的 PopupWindow
        int width = (int) (180 * getResources().getDisplayMetrics().density);
        PopupWindow popupWindow = new PopupWindow(menuLayout, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(15);
        
        // 顯示在頭像按鈕的正下方
        popupWindow.showAsDropDown(ivUserAvatar, 0, 10, Gravity.START);
    }

    /**
     * 清除本地登入狀態 (登出)
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        checkAndSyncLoginState();
        Toast.makeText(this, "帳戶已成功登出", Toast.LENGTH_SHORT).show();
    }
}