package kr.ac.chungbuk.harmonize.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
//                        .requestMatchers("/api/public/**").permitAll() // "/api/public/**" 경로는 누구나 접근 가능
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // "/api/admin/**" 경로는 ADMIN 권한 필요
//                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN") // "/api/user/**" 경로는 USER 또는 ADMIN
//                                                                                     // 권한 필요
//                        .anyRequest().authenticated() // 그 외의 모든 요청은 인증 필요
                );

//                // 폼 로그인 설정
//                .formLogin(form -> form
//                        .loginPage("/login") // 커스텀 로그인 페이지 경로
//                        .permitAll() // 로그인 페이지는 누구나 접근 가능
//                        .defaultSuccessUrl("/home", true) // 로그인 성공 시 리다이렉트할 기본 URL
//                        .failureUrl("/login?error=true") // 로그인 실패 시 리다이렉트할 URL
//                        .usernameParameter("username") // 로그인 폼에서 사용할 사용자명 파라미터 이름
//                        .passwordParameter("password") // 로그인 폼에서 사용할 비밀번호 파라미터 이름
//                )
//
//                // 로그아웃 설정
//                .logout(logout -> logout
//                        .logoutUrl("/logout") // 로그아웃 요청 경로
//                        .logoutSuccessUrl("/login?logout=true") // 로그아웃 성공 시 리다이렉트할 URL
//                        .invalidateHttpSession(true) // 세션 무효화
//                        .deleteCookies("JSESSIONID") // 쿠키 삭제
//                        .permitAll())
//
//                // 예외 처리 설정
//                .exceptionHandling(exception -> exception
//                        .accessDeniedPage("/access-denied") // 접근 거부 시 리다이렉트할 페이지
//                );

        return http.build();
    }
}
