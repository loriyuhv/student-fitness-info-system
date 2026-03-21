package com.wsw.fitnesssystem.auth.infrastructure.security.filter;

import com.wsw.fitnesssystem.auth.application.authorization.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.model.JwtUserClaims;
import com.wsw.fitnesssystem.auth.infrastructure.security.support.SecurityResponseWriter;
import com.wsw.fitnesssystem.auth.infrastructure.audit.service.LoginAuditService;
import com.wsw.fitnesssystem.auth.infrastructure.jwt.service.JwtTokenService;
import com.wsw.fitnesssystem.auth.application.port.AuthorizationCacheService;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author loriyuhv
 * @since 1.0
 * @version 1.0 2026/1/15 17:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";

    private final SecurityResponseWriter responseWriter;
    private final JwtTokenService jwtTokenService;
    private final LoginAuditService loginAuditService;
    private final SessionRepository sessionRepository;
    private final AuthorizationCacheService authorizationCacheService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        // 1. 没有 Token → 直接放行（匿名请求）：交给后面的 Security 处理
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        try {
            // 2. 解析 Access Token（验签 + exp + issuer + audience）
            JwtUserClaims claims = jwtTokenService.parseAccessToken(token);

            String tokenId = claims.getTokenId();
            Long campusId = claims.getCampusId();
            Long userId = claims.getUserId();
            String username = claims.getUsername();

            if (!StringUtils.hasText(userId.toString()) || !StringUtils.hasText(tokenId)) {
                responseWriter.write(response, ResultCode.TOKEN_INVALID);
                return;
            }

            // 3.1 检查黑名单
            boolean blacklisted = sessionRepository.isBlacklisted(tokenId);
            if (Boolean.TRUE.equals(blacklisted)) {
                loginAuditService.kick(userId, tokenId);
                responseWriter.write(response, ResultCode.TOKEN_INVALID);
                return;
            }

            // 3.2 会话是否仍在线（踢人 / 注销 / 单点）

            if (!sessionRepository.isOnline(campusId, userId, tokenId)) {
                loginAuditService.expire(userId, tokenId);
                responseWriter.write(response, ResultCode.TOKEN_INVALID);
                return;
            }

            // 4. 读取权限（只信 Redis，不信 Token）
            UserAuthorization authorization =
                authorizationCacheService.get(userId);

            if (authorization == null) {
                responseWriter.write(response, ResultCode.PERMISSION_EXPIRED);
                return;
            }

            Set<GrantedAuthority> authorities =
                authorization.permissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            // 5. 构造 Spring Security 认证对象（防止重复覆盖）
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 放入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            // 5. JWT 自然过期（能解析到旧 claims）
            Claims claims = e.getClaims();
            if (claims != null) {
                Long userId = Long.parseLong(claims.getSubject());
                String tokenId = claims.getId();
                Long campusId = claims.get("campusId", Long.class);

                // 会话失效
                sessionRepository.removeSession(campusId, userId, tokenId);

                // 审计
                loginAuditService.expire(userId, tokenId);
            }

            SecurityContextHolder.clearContext();

            responseWriter.write(response, ResultCode.TOKEN_EXPIRED);
            return;
        } catch (Exception e) {
            // 8. 兜底：非法 / 篡改 / 未知异常
            SecurityContextHolder.clearContext();

            responseWriter.write(response, ResultCode.TOKEN_INVALID);
            return;
        }

        // 9. 继续过滤器链
        filterChain.doFilter(request, response);
    }
}
