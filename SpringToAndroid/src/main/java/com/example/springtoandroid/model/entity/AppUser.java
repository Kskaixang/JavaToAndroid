package com.example.springtoandroid.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = new BigDecimal("1000000.00");

    // 自定義建構子方便初始化
    public AppUser(String username) {
        this.username = username;
        this.balance = new BigDecimal("1000000.00");
    }
}
