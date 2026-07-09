# Google Login 與 JWT 系統整合筆記 (Android + Spring Boot)

這份文件記錄了本專案 (JavaToAndroid) 如何從零開始建立 Google 登入機制，並與 Spring Boot 後端進行安全的 JWT (JSON Web Token) 身分交換。這是一份給未來開發者接手時的**核心架構與環境配置指南**。

---

## 📌 1. 核心運作邏輯
我們採用的是最安全、標準的 **「後端驗證架構」**，而非單純的前端登入：
1. **移動端 (Android)**：向 Google 請求授權，取得代表使用者身分的 `Google ID Token`。
2. **通訊過程**：Android 將這串 `Google ID Token` 打 API 發送給 Spring Boot 後端。
3. **後端 (Spring Boot)**：向 Google 伺服器驗證該 Token 的真偽，確認使用者身分 (`Subject ID`)，若為新用戶則自動註冊進資料庫 (`User` 表)。
4. **核發憑證**：後端自己簽發一把系統專屬的 `JWT` 並回傳給 Android。
5. **後續溝通**：Android 將 `JWT` 存在本地 (`TokenStorage`)，後續每次打 API (如送出委託) 都會透過 Retrofit 攔截器 (`AuthInterceptor`) 將 JWT 塞入 Header 中，後端以此辨識使用者。

---

## 📌 2. 憑證申請流程 (Google Cloud Console)

在 Google 平台上，我們需要建立**兩個**用戶端 ID，它們各自扮演不同的角色。

### 步驟 2-1：取得 Android SHA-1 指紋
要讓 Google 信任我們的 App，必須提供開發環境的 SHA-1 指紋：
1. 開啟 Android Studio。
2. 點開右側的 **Gradle** 面板。
3. 展開路徑：`app` -> `Tasks` -> `android`。
4. 點擊兩下執行 `signingReport`。
5. 在下方的 Run 視窗中，找到 `Variant: debug` 區塊，複製其中的 **SHA1** 字串。
   *(範例：`82:4B:42:84:D7:C6:8A:C8:87:62:65:AA:C9:B1:08:A3:43:F4:B2:20`)*

### 步驟 2-2：設定 OAuth 同意畫面
1. 前往 [Google Cloud Console](https://console.cloud.google.com/apis/credentials)。
2. 若尚未設定同意畫面，請點選「OAuth 同意畫面」。
3. User Type 選擇 **「外部 (External)」** -> 點擊建立。
4. 填寫必填的「應用程式名稱」與「開發人員聯絡資訊 (Email)」，其餘可預設，一路按「儲存並繼續」到底。

### 步驟 2-3：建立 Android 憑證 (允許手機彈出視窗)
這個憑證的功用是讓 Google 認得你的手機 App，允許這台裝置發起登入請求。
1. 點擊左側「憑證」 -> 上方「建立憑證」 -> **「OAuth 用戶端 ID」**。
2. 應用程式類型：選擇 **Android**。
3. 名稱：填寫如 `JavaToAndroid-Android`。
4. 套件名稱：填寫 `com.example.javatoandroid` (需與 `app/build.gradle` 的 namespace 一致)。
5. **SHA-1 憑證指紋**：填入剛才取得的 SHA1 字串。
6. *(備註：底下的「這個用戶端並非 Google Play 商店中的應用程式...」警告可忽略，也不用勾選自訂 URI 配置)*。
7. 點擊「建立」。**這個 ID 建立完後就不用理它了，不需要複製進程式碼。**

### 步驟 2-4：建立 Web 憑證 (核心驗證用的 ID)
**這是最關鍵的一步！** 雖然我們是 Android App，但為了讓後端驗證，我們必須申請 Web 類型的 ID。
1. 再次點擊「建立憑證」 -> **「OAuth 用戶端 ID」**。
2. 應用程式類型：選擇 **網頁應用程式 (Web application)**。
3. 名稱：填寫如 `JavaToAndroid-Web`。
4. **已授權的 JavaScript 來源** 與 **已授權的重新導向 URI**：請 **完全留白**，不要填寫任何東西！
5. 點擊「建立」。
6. 🎉 **請將彈出來的「用戶端 ID」複製下來，這就是我們系統中唯一要寫進程式碼的 ID！**

---

## 📌 3. 程式碼整合細節

> [!CAUTION]
> **絕對不要**把步驟 2-3 產生的 Android 類型的 Client ID 放進程式碼中。
> 雙邊系統都必須使用步驟 2-4 產生的 **Web 類型的 Client ID**！
> (運作原理：Google 會用 Android ID 的 SHA-1 來允許這台手機發起登入，然後核發這個 Web 類型專屬的 Token 回來給我們。)

### Android 端設定 (`app/build.gradle`)
我們將 Web Client ID 寫死在 `build.gradle` 中。因為 Client ID 本身就是**公開的識別碼**，沒有任何機密性，直接推送至 Git 上是絕對安全的，這樣能確保任何人 Clone 下來都能直接編譯執行。

```groovy
// app/build.gradle
android {
    defaultConfig {
        // ...
        // Google Web Client ID (直接寫死在這裡推上 Git 是完全安全且正確的做法)
        def webClientId = "548583807532-061572fv916urkah6a039aalcrjf5f06.apps.googleusercontent.com"
        buildConfigField "String", "WEB_CLIENT_ID", "\"${webClientId}\""
    }
}
```

### Spring Boot 後端設定 (`application.properties`)
後端也必須要知道這個 ID，才能在 `AuthServiceImpl` 透過 Google SDK 來驗證前端傳來的 Token 受眾 (Audience) 是否相符。

```properties
# SpringToAndroid/src/main/resources/application.properties

# JWT 密鑰 (正式機一定要用環境變數隱藏，這裡作為本地開發預設值)
jwt.secret=${JWT_SECRET:mySuperSecretKeyForJwtAuthenticationWhichShouldBeLongEnough}
jwt.expirationMs=86400000

# Google Web Client ID (與 Android 端一致)
google.client.id=548583807532-061572fv916urkah6a039aalcrjf5f06.apps.googleusercontent.com
```

### 後端接收到訂單時的行為 (`OrderController`)
因為已經過濾了 JWT，我們在 Controller 層可以輕易拿出身分：
```java
@PostMapping("/submit")
public Map<String, Object> submitOrder(@RequestBody Map<String, Object> orderData) {
    // 從 Spring Security 取得經過 JWT 解析後的使用者唯一 Google ID (Subject)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = auth.getName(); // 這裡是 "110703496874012489857"
    
    // ... 後續可透過這個唯一識別碼對應至關聯式資料庫紀錄持股
}
```

---
*文件建立日期：2026-07-09*
