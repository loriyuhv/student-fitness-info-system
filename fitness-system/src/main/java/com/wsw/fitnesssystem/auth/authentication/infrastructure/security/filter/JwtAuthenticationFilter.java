package com.wsw.fitnesssystem.auth.authentication.infrastructure.security.filter;

import com.wsw.fitnesssystem.auth.authentication.application.port.TokenPort;
import com.wsw.fitnesssystem.auth.authorization.application.service.AuthorizationQueryService;
import com.wsw.fitnesssystem.auth.authorization.application.dto.AuthorizationQuery;
import com.wsw.fitnesssystem.auth.authorization.application.dto.UserAuthorization;
import com.wsw.fitnesssystem.auth.session.domain.port.SessionRepository;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.config.SecurityProperties;
import com.wsw.fitnesssystem.auth.authentication.application.dto.AccessTokenClaims;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.security.handler.JwtAuthenticationEntryPoint;
import com.wsw.fitnesssystem.auth.authentication.infrastructure.security.model.JwtUserPrincipal;
import com.wsw.fitnesssystem.shared.context.LoginContext;
import com.wsw.fitnesssystem.shared.domain.valueobject.Operator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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
import java.util.stream.Stream;

/**
 * JWT认证过滤器
 * <p>
 * 执行流程：
 * 1. 白名单接口直接放行，跳过Token校验；
 * 2. 提取请求头 Authorization 中的 Bearer Token；无Token则放行交由SpringSecurity处理匿名访问；
 * 3. 调用JwtTokenService完成AccessToken验签、解析；底层会将Jwt原生异常转换为Spring标准AuthenticationException；
 * 4. 依次校验Token载荷完整性、版本号、黑名单、在线会话状态；校验失败抛出 {@link BadCredentialsException}；
 * 5. 从Redis加载用户权限（不信任Token内权限信息），组装认证对象写入Security上下文；
 * 6. 设置业务线程上下文 {@link LoginContext}；
 * 7. 放行过滤器链；请求结束自动清理ThreadLocal上下文；
 * </p>
 *
 * <p><b>架构说明：</b></p>
 * <li>本过滤器不再就地捕获认证异常、直接输出响应；所有认证异常向上冒泡，
 * 由 {@code JwtAuthenticationEntryPoint} 统一处理返回JSON；</li>
 * <li>使用双层finally兜底清理ThreadLocal，防止线程池复用产生上下文泄漏；</li>
 * <li>权限过期属于业务异常，本过滤器直接输出响应。</li>
 *
 * <p><b>审计边界说明</b>：</p>
 * 本过滤器不做登录审计入库。认证失败（版本失效/黑名单/不在线）只记录安全日志，
 *
 * @author loriyuhv
 * @version 1.0
 * @since 2026/1/15 17:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Token请求头前缀 */
    private static final String TOKEN_PREFIX = "Bearer ";

    /** 存放Token的Http请求头名称 */
    private static final String AUTH_HEADER = "Authorization";

    private final TokenPort tokenPort;
    private final SessionRepository sessionRepository;
    private final SecurityProperties securityProperties;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final AuthorizationQueryService authorizationQueryService;

    /**
     * 过滤器核心处理逻辑
     * @param request 请求对象
     * @param response 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. 白名单与无Token分支处理
            // 1.1 白名单优先匹配：白名单接口直接跳过JWT校验，放行后续过滤器
            String uri = request.getServletPath();
            boolean isPermitAll = securityProperties.getPermitAllPatterns().stream()
                    .anyMatch(p -> p.matches(uri));
            if (isPermitAll) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader(AUTH_HEADER);

            // 1.2 请求头不存在有效Bearer Token：放行交由SpringSecurity处理匿名访问
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 截取Bearer前缀后的原始token字符串
            String token = authHeader.substring(TOKEN_PREFIX.length());

            // 2. 解析AccessToken，完成验签、过期、签发者、受众校验
            // 底层JwtTokenService会将原生Jwt异常转换为AuthenticationException，向上抛出
            AccessTokenClaims claims = tokenPort.parseAccessToken(token);

            String tokenId = claims.getJti();
            Long userId = claims.getUserId();
            Long campusId = claims.getCampusId();
            String username = claims.getUsername();
            Integer userType = claims.getUserType();
            Long tokenVersion = claims.getTokenVersion();
            Operator operator = new Operator(campusId, userId, username, userType);

            // 校验Token必要载荷是否缺失
            if (userId == null || campusId == null || !StringUtils.hasText(tokenId)) {
                // 属于凭证非法，主动抛出标准认证异常
                throw new BadCredentialsException("Token缺失必要载荷信息");
            }

            // 3.1 Token版本校验：版本不匹配代表密码修改、强制下线，Token失效
            // 获取 Redis 中的当前版本号
            long currentVersion = sessionRepository.getTokenVersion(operator);
            // 版本号不一致 → 失效（密码已改）
            if (tokenVersion != currentVersion) {
                log.warn("[安全审计] Token版本失效: uri={}, userId={}, tokenId={}, " +
                    "reason=VERSION_MISMATCH", uri, userId, tokenId);
                throw new BadCredentialsException("Token版本已失效");
            }

            // 3.2 黑名单校验：注销/主动踢人后的Token加入黑名单
            boolean blacklisted = sessionRepository.isBlacklisted(tokenId);
            if (blacklisted) {
                log.warn("[安全审计] Token黑名单拒绝: uri={}, userId={}, tokenId={}, " +
                    "reason=BLACKLISTED", uri, userId, tokenId);
                throw new BadCredentialsException("Token已加入黑名单");
            }

            // 3.3 会话在线校验：实现单点登录、会话下线控制
            if (!sessionRepository.isOnline(operator, tokenId)) {
                log.warn("[安全审计] Token不在线: uri={}, userId={}, tokenId={}," +
                    " reason=SESSION_OFFLINE", uri, userId, tokenId);
                throw new BadCredentialsException("会话已下线");
            }

            // 4. 加载用户权限：权限信任Redis缓存，不直接使用Token内携带权限
            UserAuthorization authorization =
                    authorizationQueryService.authorize(
                            AuthorizationQuery.builder().userId(userId).campusId(campusId).build());

            // 组装SpringSecurity权限集合：角色自动添加ROLE_前缀，权限标识原样保留
            Set<GrantedAuthority> authorities = Stream.concat(
                    // 先处理角色，加上ROLE_前缀
                    authorization.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role)),
                    // 再处理权限，不加前缀
                    authorization.getPermissions().stream()
                            .map(SimpleGrantedAuthority::new)
            ).collect(Collectors.toSet());

            // 5. 构造认证信息存入Security上下文（避免重复覆盖已有认证对象）
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                JwtUserPrincipal principal = new JwtUserPrincipal(
                        username, tokenId, campusId, userId
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, authorities
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 放入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 设置业务ThreadLocal上下文，供业务代码直接获取登录用户信息
                LoginContext.setOperator(operator);
            }

            // 6. 继续过滤器链
            try {
                filterChain.doFilter(request, response);
            } finally {
                // 请求正常走完链路后清理上下文
                LoginContext.clear();
                SecurityContextHolder.clearContext();
            }
        } catch (AuthenticationException e) {
            authenticationEntryPoint.commence(request, response, e);
        }
        finally {
            // 兜底清理：在校验中途抛出异常时，内层finally无法执行，防止ThreadLocal线程复用泄露
            LoginContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

}
