package com.example.javatoandroid.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.javatoandroid.factory.WebSocketClientFactory;
import com.example.javatoandroid.ui.activity.LoginActivity;
import com.example.javatoandroid.websocket.IQuoteWebSocketClient;

/**
 * 統一控管登出與環境清理的服務
 */
public class SessionCleanManager {

    private static final String PREF_NAME = "UserPrefs"; // MainActivity 舊版的 mock preferences

    /**
     * 執行完整的環境清理，並回傳跳轉至 LoginActivity 的 Intent
     * @param context
     * @return 帶有清空返回棧 (Backstack) 標籤的 Intent
     */
    public static Intent clearSessionAndGetIntent(Context context) {
        // 1. 清空 TokenStorage (真正的 JWT 與 Google Token)
        new TokenStorage(context).clearToken();

        // 2. 清空舊版的 SharedPreferences 模擬狀態
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // 3. 登出 Google 帳號，讓下次登入強制重新選擇帳號或重新授權
        GoogleSignInHelper googleSignInHelper = new GoogleSignInHelper(context);
        googleSignInHelper.signOut();

        // 4. 斷開底層的 WebSocket 即時報價連線，避免背景持續耗電或收到錯誤資料
        IQuoteWebSocketClient wsClient = WebSocketClientFactory.getClient();
        if (wsClient != null) {
            wsClient.disconnect();
        }

        // 5. 準備跳轉回登入頁的 Intent
        Intent intent = new Intent(context, LoginActivity.class);
        // 設定 FLAG_ACTIVITY_NEW_TASK 與 FLAG_ACTIVITY_CLEAR_TASK
        // 這會將現有的 Activity (例如 MainActivity) 全部從記憶體中移除，確保使用者按下實體返回鍵不會再回到 MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }
}
