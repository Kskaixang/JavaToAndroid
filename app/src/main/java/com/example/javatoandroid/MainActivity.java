package com.example.javatoandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javatoandroid.model.entity.ApiResponse;
import com.example.javatoandroid.model.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 宣告類別，MainActivity 繼承自 AppCompatActivity (Android 的標準 Activity 視窗基底)
public class MainActivity extends AppCompatActivity {

    // 宣告畫面上用來顯示時間的文字元件 (TextView)
    private TextView timeTextView;
    // 宣告使用者點擊觸發連線的按鈕元件 (Button)
    private Button fetchTimeButton;
    // 宣告自訂定義 API 連線介面的 ApiService 變數
    private ApiService apiService;
    // 設定為 true 代表「本機模擬器測試」；設定為 false 代表「連線到 Oracle 雲端伺服器」
    private static final boolean IS_LOCAL_TEST = false;
    // 當 Activity 視窗被建立時，Android 系統會自動觸發這個生命週期方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 根據開關動態決定連線網址
        String baseUrl = IS_LOCAL_TEST ? "http://10.0.2.2:8080/" : "http://161.33.157.67:8088/";

        // 呼叫父類別的 onCreate 方法，執行視窗的基礎系統初始化
        super.onCreate(savedInstanceState);
        // 設定這個 Activity 所要使用的 XML 版面配置檔案 (對應 activity_main.xml)
        setContentView(R.layout.activity_main);

        // 透過元件的 ID 找到 XML 中的文字元件，並指派給變數 timeTextView
        timeTextView = findViewById(R.id.myTextView);
        // 透過元件的 ID 找到 XML 中的按鈕元件，並指派給變數 fetchTimeButton
        fetchTimeButton = findViewById(R.id.myButton);

        // 使用 Retrofit.Builder 建構器，用來配置網路連線的各項基礎設定
        Retrofit retrofit = new Retrofit.Builder()
                // 設定 API 連線的主機網址 (必須以斜線 / 結尾，指向您的 Oracle 伺服器公網 IP)
//                .baseUrl("http://161.33.157.67:8088/")
                .baseUrl(baseUrl)
                // 指定使用 Gson 作為 JSON 解析器，將伺服器傳回的 JSON 字串自動轉換成 Java 物件
                .addConverterFactory(GsonConverterFactory.create())
                // 呼叫 build() 正式建立出配置完畢的 Retrofit 連線實例
                .build();

        // 透過 Retrofit 自動產生一個實作了 ApiService 介面的代理物件，供後續發起網路請求
        apiService = retrofit.create(ApiService.class);

        // 設定按鈕的點擊監聽器，當使用者點擊該按鈕時，會執行括號內的 Lambda 程式碼
        fetchTimeButton.setOnClickListener(v -> {
            // 呼叫我們自訂的 fetchTimeFromServer 方法發起網路請求
            fetchTimeFromServer();
        });
    }

    // 自訂方法：發起網路請求並解析回傳的時間資料
    private void fetchTimeFromServer() {
        // 呼叫 apiService 定義好的連線方法，獲取一個包裝好的 Call 物件 (代表一次待執行的網路請求)
        Call<ApiResponse<String>> call = apiService.getCurrentTime();

        // 使用 enqueue 將請求加入非同步連線佇列，這會在「背景執行緒」中默默執行網路連線
        // 這樣做能確保手機的主畫面 UI 不會因為網路延遲而發生「畫面卡死/無回應 (ANR)」的問題
        call.enqueue(new Callback<ApiResponse<String>>() {

            // 當網路請求成功發出，且順利獲得伺服器的 HTTP 回應時，會自動觸發此方法
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                // 1. 檢查 HTTP 狀態碼是否成功 (200~299 之間)，且回應的內容物 (body) 不為空值
                if (response.isSuccessful() && response.body() != null) {

                    // 2. 獲取自動由 Gson 解析轉換完畢的 ApiResponse 物件 (泛型填 String，代表 data 裝載的是字串)
                    ApiResponse<String> apiResponse = response.body();

                    // 3. 取得 API 回傳物件中的自訂狀態碼 (後端設定的 status，例如 200)
                    int status = apiResponse.getStatus();

                    // 4. 取得 API 回傳物件中的提示訊息 (後端設定的 message，例如 "時間獲取成功")
                    String message = apiResponse.getMessage();

                    // 5. 取得 API 物件中實際裝載的內容 (後端設定的 data，也就是台北時間的字串)
                    String timeData = apiResponse.getData();

                    // 6. 檢查後端自訂的 status 是否為 200 (代表後端的資料邏輯正確無誤)
                    if (status == 200) {
                        // 7. 將成功解析出來的時間字串設定到畫面的 TextView 元件上顯示
                        timeTextView.setText(timeData);
                    } else {
                        // 8. 若後端傳回的不是 200，在 TextView 顯示後端回傳的錯誤描述
                        timeTextView.setText("API 內部錯誤: " + message);
                    }
                } else {
                    // 9. 若 HTTP 連線代碼不成功 (例如伺服器回傳 404 或 500)，在 TextView 顯示 HTTP 錯誤代碼
                    timeTextView.setText("HTTP 連線錯誤代碼: " + response.code());
                }
            }

            // 當網路完全連線失敗時 (例如：手機沒開網、Wi-Fi 斷線、伺服器宕機等) 會自動觸發此方法
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // 1. 在畫面的 TextView 上顯示網路連線失敗的具體異常訊息
                timeTextView.setText("連線失敗: " + t.getMessage());
                // 2. 同時將詳細的錯誤堆疊資訊輸出至 Android Studio 的 Logcat 中，供開發人員排錯
                Log.e("API_ERROR", "連線伺服器時發生例外狀況：", t);
            }
        });
    }
}