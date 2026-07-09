package com.example.springtoandroid.repository;

import com.example.springtoandroid.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 資料庫存取介面
 * 繼承 JpaRepository 即可自動獲得基本的 CRUD 能力。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 透過 Google ID 尋找使用者
     * 用於登入時確認該使用者是否已經註冊過。
     *
     * @param googleId Google 發放的唯一識別碼
     * @return 包裝在 Optional 中的 User 物件 (可能為空)
     */
    Optional<User> findByGoogleId(String googleId);
}
