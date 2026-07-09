package com.example.javatoandroid.utils;

import android.content.Context;
import android.content.Intent;

import com.example.javatoandroid.BuildConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * 封裝 Google Sign-In SDK 的工具類別
 * 負責產生 Google 登入的 Intent
 */
public class GoogleSignInHelper {

    private GoogleSignInClient mGoogleSignInClient;

    public GoogleSignInHelper(Context context) {
        // 設定 Google 登入選項：要求使用者的 ID 與 Email，並且索取 Server Auth Code 或 ID Token (這裡我們需要 ID Token)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID) // 這裡帶入從 local.properties 讀到的 WEB_CLIENT_ID
                .requestEmail()
                .build();

        // 建立 GoogleSignInClient 實體
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    /**
     * 取得登入用的 Intent
     * 在 Activity 中應使用 startActivityForResult 或 ActivityResultLauncher 來啟動
     */
    public Intent getSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }
    
    /**
     * 登出 Google 帳號 (清除本地登入狀態)
     */
    public void signOut() {
        mGoogleSignInClient.signOut();
    }
}
