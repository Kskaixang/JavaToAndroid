package com.example.javatoandroid;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // onCreate 是這個畫面的「入口方法」，相當於應用程式剛載入這個頁面時的初始化區塊
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 啟用滿版螢幕效果 (讓 UI 延伸至最上方狀態列和最下方導航列)
        EdgeToEdge.enable(this);

        // 載入剛剛編輯的佈局檔案 activity_main.xml (R 代表 Resource 資源倉庫)
        setContentView(R.layout.activity_main);

        // 這段是系統預設產生的：設定系統狀態列與導航列的邊距限制，防止畫面元件被遮擋
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // ==========================================
        // 這裡開始寫我們自訂的按鈕點擊邏輯
        // ==========================================
        // 1. 透過 findViewById 尋找 XML 中定義的 ID，將它們與 Java 中的變數做綁定
        // 這步驟就像是 JavaScript 中的 document.getElementById()
        TextView myTextView = findViewById(R.id.myTextView);
        Button myButton = findViewById(R.id.myButton);
        // 2. 幫按鈕設定「點擊監聽器 (OnClickListener)」
        // 當使用者在手機畫面上用手指按這個按鈕時，onClick() 裡面的程式碼就會被呼叫
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 當按鈕被點擊，把 TextView 的文字設定為 "Hello World!"
                myTextView.setText("Hello World!");
            }
        });
    }
}