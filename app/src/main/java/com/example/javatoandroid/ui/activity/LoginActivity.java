package com.example.javatoandroid.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javatoandroid.R;
import com.example.javatoandroid.service.AuthService;
import com.example.javatoandroid.service.impl.AuthServiceImpl;
import com.example.javatoandroid.repository.AuthRepository;
import com.example.javatoandroid.utils.GoogleSignInHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private GoogleSignInHelper googleSignInHelper;
    private AuthService authService;

    private TextView tvStatus;
    private Button btnGoogleSignIn;
    private ProgressBar progressBar;

    // 定義 ActivityResultLauncher，取代舊版的 startActivityForResult
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 從結果中提取 Task
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvStatus = findViewById(R.id.tvStatus);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);

        googleSignInHelper = new GoogleSignInHelper(this);
        authService = new AuthServiceImpl(this);

        // 檢查是否已經有登入的 Token
        com.example.javatoandroid.utils.TokenStorage tokenStorage = new com.example.javatoandroid.utils.TokenStorage(this);
        if (tokenStorage.isLoggedIn()) {
            Log.d(TAG, "已偵測到可用 Token，自動導向主畫面");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnGoogleSignIn.setOnClickListener(v -> {
            // 點擊登入時，發起 Intent
            Intent signInIntent = googleSignInHelper.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // 取得 Google 帳號與 Token
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            
            if (idToken != null) {
                // 1. Google 登入成功，拿到 ID Token
                Log.d(TAG, "成功獲取 Google ID Token");
                tvStatus.setText("Google 授權成功，正在與後端交換 Token...");
                setLoading(true);

                // 儲存 Google 個人資料 (頭像與名稱)
                String name = account.getDisplayName();
                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                new com.example.javatoandroid.utils.TokenStorage(this).saveProfile(name, photoUrl);

                // 2. 呼叫後端 API，將 Google Token 換成系統的 JWT
                authService.exchangeGoogleTokenForJwt(idToken, new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "登入成功！", Toast.LENGTH_SHORT).show();
                            
                            // 登入成功後跳轉至主畫面 MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 關閉登入頁面
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            tvStatus.setText("認證失敗:\n" + message);
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            // 後端驗證失敗時，也可考慮呼叫 googleSignInHelper.signOut() 將 Google 狀態登出
                            googleSignInHelper.signOut();
                        });
                    }
                });

            } else {
                Log.e(TAG, "獲取 ID Token 失敗，請確認是否配置了 WEB_CLIENT_ID");
                Toast.makeText(this, "無法獲取登入憑證", Toast.LENGTH_SHORT).show();
            }
            
        } catch (ApiException e) {
            // Google Sign In 失敗 (例如使用者取消、或是 SHA-1 設定錯誤)
            Log.w(TAG, "Google 登入失敗, statusCode=" + e.getStatusCode());
            Toast.makeText(this, "Google 登入失敗 (代碼:" + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!isLoading);
    }
}
