package com.example.springtoandroid.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 系統使用者實體 (Entity)
 * 對應至資料庫中的 `users` 資料表。
 * 使用 Lombok 自動產生 Getter, Setter 與建構子。
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 系統內部自動產生的主鍵 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Google 帳號的唯一識別碼 (Subject ID)
     * 因為是登入的關鍵憑證，加上 unique = true 確保唯一性
     */
    @Column(unique = true, nullable = false)
    private String googleId;

    /**
     * 使用者信箱
     */
    @Column(nullable = false)
    private String email;

    /**
     * 使用者顯示名稱
     */
    private String name;

    /**
     * 使用者的大頭貼網址
     */
    private String avatarUrl;

}
