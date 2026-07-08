package com.example.javatoandroid.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

/**
 * 專責監聽與管理系統網路狀態的工具類別 (Network Monitor)
 * - 負責與 Android 底層 ConnectivityManager 交握
 * - 透過 Callback 將連線/斷線事件通知給 UI 層
 */
public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final NetworkStateListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean isConnected = true;

    /**
     * 定義回呼介面，讓外部 (例如 Activity) 能夠實作網路狀態變化的處理邏輯
     */
    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    public NetworkMonitor(Context context, NetworkStateListener listener) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    /**
     * 開始監聽網路狀態
     */
    public void startListening() {
        if (connectivityManager == null) return;

        // 初始化檢查：啟動當下如果沒有網路，立即通知 onNetworkLost
        Network currentNetwork = connectivityManager.getActiveNetwork();
        if (currentNetwork == null) {
            isConnected = false;
            runOnMainThread(() -> listener.onNetworkLost());
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // 只有從斷線變成連線時，才通知 UI
                if (!isConnected) {
                    isConnected = true;
                    runOnMainThread(() -> listener.onNetworkAvailable());
                }
            }

            @Override
            public void onLost(Network network) {
                // Android 模擬器常常會有多張虛擬網卡，一張斷線不代表全部斷線
                // 所以我們必須再次確認是不是真的「所有網路」都沒了
                Network active = connectivityManager.getActiveNetwork();
                if (active == null) {
                    if (isConnected) {
                        isConnected = false;
                        runOnMainThread(() -> listener.onNetworkLost());
                    }
                }
            }
        };

        // 使用 registerDefaultNetworkCallback 更能準確反映「當前正在使用的主要網路」
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    /**
     * 停止監聽網路狀態，避免 Memory Leak
     */
    public void stopListening() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    /**
     * 確保回呼總是在主執行緒 (UI Thread) 上執行，方便調用者直接修改畫面
     */
    private void runOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
