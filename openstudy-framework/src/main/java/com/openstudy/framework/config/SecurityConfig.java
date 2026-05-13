package com.openstudy.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;
import com.openstudy.framework.config.properties.PermitAllUrlProperties;
import com.openstudy.framework.security.filter.JwtAuthenticationTokenFilter;
import com.openstudy.framework.security.handle.AuthenticationEntryPointImpl;
import com.openstudy.framework.security.handle.LogoutSuccessHandlerImpl;

@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig
{
    @Autowired
    private AuthenticationEntryPointImpl unauthorizedHandler;

    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Autowired
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception
    {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .headers((headersCustomizer) -> {
                    headersCustomizer.cacheControl(cache -> cache.disable()).frameOptions(options -> options.sameOrigin());
                })
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> {
                    // 使用配置文件中的白名单
                    permitAllUrl.getUrls().forEach(url -> requests.requestMatchers(url).permitAll());
                    // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                    requests.requestMatchers("/login", "/register", "/captchaImage").permitAll()
                            // ✅ 关键：直接添加 AI 接口白名单（所有 HTTP 方法）
                            .requestMatchers("/ai/**").permitAll()
                            .requestMatchers("/ocr/**").permitAll()
                            .requestMatchers("/document/**").permitAll()
                            .requestMatchers("/test/**").permitAll()
                            .requestMatchers("/rag/**").permitAll()
                            // ✅ WebSocket 接口，允许匿名访问
                            .requestMatchers("/ws/**").permitAll()
                            // ✅ 公开笔记接口，允许匿名访问
                            .requestMatchers("/notes/note/public/**").permitAll()
                            .requestMatchers("/notes/note/publicSection/**").permitAll()
                            // ✅ 明确指定 DELETE 方法的白名单（防止方法级别限制）
                            .requestMatchers(HttpMethod.DELETE, "/rag/document/**").permitAll()
                            .requestMatchers(HttpMethod.DELETE, "/rag/knowledgeBase/**").permitAll()
                            // 静态资源，可匿名访问
                            .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/profile/**").permitAll()
                            .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/druid/**").permitAll()
                            // 除上面外的所有请求全部需要鉴权认证
                            .anyRequest().authenticated();
                })
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class)
                .addFilterBefore(corsFilter, LogoutFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}