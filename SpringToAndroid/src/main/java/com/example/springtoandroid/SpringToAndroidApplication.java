package com.example.springtoandroid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 【這是正確的 4.x 新路徑】
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class SpringToAndroidApplication {

    public static void main(String[] args) {
        // 2. 改為手動建立 SpringApplication 實例
        SpringApplication app = new SpringApplication(SpringToAndroidApplication.class);

        // [關鍵修復] 顯式註冊自動 SSH 通道啟動器，確保它在 HikariCP 之前 100% 執行
        app.addInitializers(new com.example.springtoandroid.config.SshTunnelInitializer());

        // 3. 強制指定運行模式為 Web Servlet 伺服器
        app.setWebApplicationType(WebApplicationType.SERVLET);

        // 4. 啟動
        app.run(args);
    }


}