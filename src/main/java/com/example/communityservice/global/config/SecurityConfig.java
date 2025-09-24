package com.example.communityservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Spring Security 설정 익명 사용자 비밀번호 암호화를 위한 최소한의 설정 (인증은 API Gateway에서 처리) */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // BCryptPasswordEncoder Bean 등록 (익명 사용자 비밀번호 암호화용)
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 모든 요청 허용 (MSA 환경에서는 API Gateway가 인증 처리)
  /**
   * CSRF를 비활성화하는 이유
   *
   * <p>1. REST API는 stateless: 세션을 사용하지 않으므로 CSRF 공격 대상이 아님
   *
   * <p>2. 토큰 기반 인증: JWT 등을 사용할 때 CSRF 토큰이 불필요
   *
   * <p>3. MSA 환경: API Gateway에서 인증 처리하므로 개별 서비스에서는 불필요
   *
   * <p>4. 클라이언트가 주로 SPA나 모바일: 전통적인 폼 기반 웹이 아님
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (REST API는 stateless하므로 불필요)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }
}
