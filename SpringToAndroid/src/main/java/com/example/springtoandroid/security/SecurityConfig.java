package com.example.springtoandroid.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 停用 CSRF，因為我們使用 JWT
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 開放 Google 登入端點
                .requestMatchers("/api/auth/google-login").permitAll()
                // 開放 H2 Console (若有需要)
                .requestMatchers("/h2-console/**").permitAll()
                // 開放 websocket 端點 (若有需要)
                .requestMatchers("/ws/**").permitAll()
                // 其他所有請求都需要登入驗證
                .anyRequest().authenticated()
            );

        // 如果有開啟 H2 Console，需關閉 X-Frame-Options 限制
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        // 將自訂的 JWT 過濾器加在 Spring Security 的 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
