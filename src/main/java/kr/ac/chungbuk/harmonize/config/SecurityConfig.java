package kr.ac.chungbuk.harmonize.config;

import kr.ac.chungbuk.harmonize.security.JwtAuthenticationEntryPoint;
import kr.ac.chungbuk.harmonize.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    JwtAuthenticationFilter jwtRequestFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAuthenticationFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public DefaultSecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    // MusicController
                    auth.requestMatchers(HttpMethod.POST, "/api/music").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/music/{musicId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/music/{musicId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/music/bulk").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/music/{musicId}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/search").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/rank").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/recent").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/first-feedback").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.GET, "/api/music/theme").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/theme/music").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/count").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/albumcover/{filename}").permitAll();

                    // MusicAnalysisController
                    auth.requestMatchers(HttpMethod.POST, "/api/music/{musicId}/files").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/music/bulk/files").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/music/{musicId}/analyze").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/music/{musicId}/delete").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/music/audio/{filename}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/pitch/{musicId}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/music/pitch/audio/{musicId}").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/music/recsys/content-based").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/music/recsys/collaborative").hasAuthority("ADMIN");

                    // MusicActionController
                    auth.requestMatchers(HttpMethod.POST, "/api/music/{musicId}/like").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/music/{musicId}/like").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.GET, "/api/music/bookmarked").hasAuthority("USER");
                    auth.requestMatchers(HttpMethod.POST, "/api/music/{musicId}/feedback").hasAuthority("USER");

                    // ArtistController
                    auth.requestMatchers(HttpMethod.POST, "/api/artist").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/artist/{artistId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/artist/{artistId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/artist").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/artist/{artistId}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/artist/count").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/artist/profile/{filename}").permitAll();

                    // GroupController
                    auth.requestMatchers(HttpMethod.POST, "/api/group").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/group/{groupId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/group/{groupId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/group").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/group/{groupId}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/group/profile/{filename}").permitAll();

                    // UserController
                    auth.requestMatchers(HttpMethod.POST, "/api/user").permitAll();
                    auth.requestMatchers(HttpMethod.PUT, "/api/user/{userId}").hasAnyAuthority("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/user/admin/{userId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/user/{userId}").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/user/{userId}").hasAnyAuthority("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/user").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/user/login").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/user/logout").hasAnyAuthority("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/user/auth/currentuser").hasAnyAuthority("USER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/user/count").hasAuthority("ADMIN");

                    // UserAnalysisController
                    auth.requestMatchers(HttpMethod.POST, "/api/user/analysis").hasAuthority("USER");

                    // LogController
                    auth.requestMatchers(HttpMethod.GET, "/api/log/bulk").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/log/bulk").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/log/bulk/files").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/log/bulk/files").hasAuthority("ADMIN");

                    // 기타 모든 요청 차단
                    auth.anyRequest().denyAll();
                })
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    // 비밀번호 암호화용 Encoder 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
