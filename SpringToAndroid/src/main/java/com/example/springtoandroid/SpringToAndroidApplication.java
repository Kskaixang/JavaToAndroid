package com.example.springtoandroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 【這是正確的 4.x 新路徑】
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

// 排除 DataSourceAutoConfiguration，這樣即使沒有設定資料庫，程式也能順利啟動
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SpringToAndroidApplication {

    public static void main(String[] args) {
        // 2. 改為手動建立 SpringApplication 實例
        SpringApplication app = new SpringApplication(SpringToAndroidApplication.class);

        // 3. 強制指定運行模式為 Web Servlet 伺服器
        app.setWebApplicationType(WebApplicationType.SERVLET);

        // 4. 啟動
        app.run(args);
    }


}