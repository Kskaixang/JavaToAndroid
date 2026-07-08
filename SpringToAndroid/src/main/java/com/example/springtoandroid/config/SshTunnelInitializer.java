package com.example.springtoandroid.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 在 Spring Boot 啟動初期 (連接資料庫之前) 自動建立 SSH 通道
 * 僅在 Windows 開發環境下生效
 */
public class SshTunnelInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static Process sshProcess;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 檢查是否為 Windows 環境 (通常代表開發機)
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                // 檢查 Port 3307 是否已經被佔用 (防止重複啟動)
                Process checkProcess = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :3307").start();
                if (checkProcess.waitFor() != 0) {
                    System.out.println("=================================================");
                    System.out.println("🚀 [Auto SSH] 偵測到本機開發環境，準備啟動 SSH 通道...");
                    
                    // 取得金鑰路徑 (支援跨電腦、不同路徑，並記錄在電腦本地)
                    String keyPath = getOrAskForKeyPath();
                    if (keyPath == null || keyPath.isEmpty()) {
                        System.err.println("❌ [Auto SSH] 取消選取金鑰，放棄建立 SSH 通道。程式可能會因為連不到資料庫而啟動失敗。");
                        return;
                    }
                    
                    System.out.println("🔑 [Auto SSH] 使用金鑰: " + keyPath);
                    System.out.println("=================================================");

                    // 執行 SSH 背景連線
                    ProcessBuilder pb = new ProcessBuilder(
                            "ssh", "-N", "-L", "3307:127.0.0.1:3306", 
                            "-i", keyPath, 
                            "-o", "StrictHostKeyChecking=no", 
                            "opc@161.33.157.67"
                    );
                    sshProcess = pb.start();

                    // 暫停 2 秒，確保 SSH 通道完全打通，資料庫才不會連線失敗
                    Thread.sleep(2000);

                    // 註冊關閉掛鉤：當 Spring Boot 停止時，自動砍掉這個 SSH 行程
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (sshProcess != null && sshProcess.isAlive()) {
                            System.out.println("🛑 [Auto SSH] 正在關閉 SSH 通道...");
                            sshProcess.destroy();
                        }
                    }));
                } else {
                    System.out.println("✅ [Auto SSH] Port 3307 已被佔用，跳過通道建立。");
                }
            } catch (Exception e) {
                System.err.println("❌ [Auto SSH] 建立 SSH 通道失敗: " + e.getMessage());
            }
        }
    }

    /**
     * 讀取本地快取的金鑰路徑。若無，則彈出視窗請使用者選擇。
     */
    private String getOrAskForKeyPath() {
        // 將選擇紀錄存在使用者家目錄下，不進 Git，每台電腦都能有自己的設定
        Path configPath = Paths.get(System.getProperty("user.home"), ".springtoandroid_ssh_key");
        String keyPath = "";

        if (Files.exists(configPath)) {
            try {
                keyPath = new String(Files.readAllBytes(configPath)).trim();
                // 檢查該路徑的檔案是否還存在
                if (Files.exists(Paths.get(keyPath))) {
                    return keyPath;
                }
            } catch (IOException ignored) {}
        }

        // 如果找不到或檔案遺失，則暫時關閉 Headless 模式，呼叫系統原生選檔視窗
        String originalHeadless = System.getProperty("java.awt.headless");
        System.setProperty("java.awt.headless", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("請選擇 Oracle Cloud 的 SSH 金鑰檔案 (.key 或 .pem)");
            
            // 強制讓視窗置頂，避免被 IntelliJ 擋住
            JDialog dialog = new JDialog();
            dialog.setAlwaysOnTop(true);
            
            int result = fileChooser.showOpenDialog(dialog);
            dialog.dispose();

            if (result == JFileChooser.APPROVE_OPTION) {
                keyPath = fileChooser.getSelectedFile().getAbsolutePath();
                // 記住選擇，下次就不用再選了
                Files.write(configPath, keyPath.getBytes());
                return keyPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 恢復原本的 Headless 狀態
            if (originalHeadless != null) {
                System.setProperty("java.awt.headless", originalHeadless);
            } else {
                System.clearProperty("java.awt.headless");
            }
        }

        return null;
    }
}
