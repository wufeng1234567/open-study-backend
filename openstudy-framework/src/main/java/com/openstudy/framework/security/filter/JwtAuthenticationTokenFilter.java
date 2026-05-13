package com.openstudy.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.openstudy.common.core.domain.model.LoginUser;
import com.openstudy.common.utils.SecurityUtils;
import com.openstudy.common.utils.StringUtils;
import com.openstudy.framework.config.properties.PermitAllUrlProperties;
import com.openstudy.framework.web.service.TokenService;

/**
 * token过滤器 验证token有效性
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PermitAllUrlProperties permitAllUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        log.info("🔍 请求路径: {} {}", method, uri);  // 加这一行


        // 1. 检查是否是白名单路径，如果是则直接放行
        if (isPermitAllUrl(uri)) {
            log.debug("✅ 白名单路径，跳过 JWT 验证: {} {}", method, uri);
            chain.doFilter(request, response);
            return;
        }

        log.debug("🔐 需要 JWT 验证: {} {}", method, uri);
        
        // 2. 非白名单路径，执行 JWT token 验证
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
        {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("✅ JWT 验证成功: userId={}", loginUser.getUserId());
        }
        else if (StringUtils.isNull(loginUser)) {
            log.warn("⚠️ 未找到有效的 JWT Token: {} {}", method, uri);
        }
        
        chain.doFilter(request, response);
    }

    /**
     * 检查是否是白名单路径
     */
    private boolean isPermitAllUrl(String uri) {
        // 检查静态配置的白名单（@Anonymous 注解）
        for (String url : permitAllUrl.getUrls()) {
            if (matchUrl(url, uri)) {
                return true;
            }
        }
        
        // 检查硬编码的白名单（与 SecurityConfig 保持一致）
        String[] hardcodedWhitelist = {
            "/login", "/register", "/captchaImage",
            "/ai/", "/ocr/", "/test/", "/rag/",
            "/swagger-ui.html", "/v3/api-docs/", "/swagger-ui/", "/druid/"
        };
        
        for (String pattern : hardcodedWhitelist) {
            if (uri.startsWith(pattern) || uri.equals(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 简单的 URL 匹配（支持 * 通配符）
     */
    private boolean matchUrl(String pattern, String uri) {
        if (pattern.contains("*")) {
            String regex = pattern.replace("**", ".*").replace("*", "[^/]*");
            return uri.matches(regex);
        }
        return pattern.equals(uri);
    }
}
