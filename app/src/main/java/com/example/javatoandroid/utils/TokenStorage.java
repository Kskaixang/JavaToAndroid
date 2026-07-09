package com.example.javatoandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 負責儲存與管理 JWT Token 的工具類別
 * 使用 Android 的 SharedPreferences 將 Token 存在本地，
 * 供發送 API 時(AuthInterceptor) 隨時取用。
 */
public class TokenStorage {

    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private SharedPreferences prefs;

    public TokenStorage(Context context) {
        // 使用 Context.MODE_PRIVATE 確保只有我們的 App 可以存取這個檔案
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 儲存後端發下來的 JWT
     */
    public void saveToken(String token) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply();
    }

    /**
     * 取得目前儲存的 JWT
     * @return Token 字串，若無則回傳 null
     */
    public String getToken() {
        return prefs.getString(KEY_JWT_TOKEN, null);
    }

    /**
     * 登出時清除 Token 與使用者資料
     */
    public void clearToken() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_NAME)
            .remove(KEY_AVATAR_URL)
            .apply();
    }

    /**
     * 儲存 Google 使用者個人資料 (供首頁顯示頭像與名稱)
     */
    public void saveProfile(String name, String avatarUrl) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    /**
     * 檢查是否處於登入狀態 (有沒有 Token)
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
