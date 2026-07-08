package com.example.javatoandroid.ui.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

/**
 * 網路斷線時的專用警告彈出視窗
 * - 負責統一的網路中斷 UI 呈現邏輯
 */
public class NetworkWarningDialog {

    private AlertDialog dialog;

    public NetworkWarningDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("⚠️ 網路連線中斷");
        builder.setMessage("偵測不到網路連線，請檢查您的 Wi-Fi 或行動網路。連線恢復後將自動重新整理。");
        builder.setCancelable(false); // 禁止使用者點擊旁邊取消，強制必須要有網路
        
        // 允許手動關閉，但如果沒網路，功能依然會無法使用
        builder.setPositiveButton("我知道了", (d, which) -> d.dismiss());
        
        dialog = builder.create();
    }

    /**
     * 顯示警告視窗
     */
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * 關閉警告視窗
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 檢查視窗是否正在顯示中
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
