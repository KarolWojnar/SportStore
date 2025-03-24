package org.shop.sportwebstore.service.user;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsImplService userDetailsService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.exp}") private int accessTokenExpiryMs;
    @Value("${jwt.refresh.exp}") private int refreshTokenExpiryMs;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String accessToken;
        String username = null;
        boolean shouldRefresh = false;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            if (jwtUtil.isBlackListed(accessToken)) {
                log.info("Access token is blacklisted.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access token is blacklisted.");
                return;
            }
            try {
                username = jwtUtil.extractUsername(accessToken);

                Date expiration = jwtUtil.extractExpiration(accessToken);
                long timeLeft = expiration.getTime() - System.currentTimeMillis();
                if (timeLeft < accessTokenExpiryMs * 0.8) {
                    shouldRefresh = true;
                    log.debug("Access token nearing expiration, will refresh");
                }

            } catch (ExpiredJwtException e) {
                shouldRefresh = true;
                log.info("Access token expired - requiring refresh");
            }
        }

        if (shouldRefresh) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("Refresh-token".equals(cookie.getName())) {
                        String refreshToken = cookie.getValue();
                        try {
                            username = jwtUtil.getSubject(refreshToken);
                            UserDetails user = userDetailsService.loadUserByUsername(username);

                            if (jwtUtil.validateToken(refreshToken, user)) {
                                String newAccessToken = jwtUtil.generateToken(username, accessTokenExpiryMs);
                                response.setHeader("Authorization", "Bearer " + newAccessToken);

                                Date refreshExpiration = jwtUtil.extractExpiration(refreshToken);
                                long refreshTimeLeft = refreshExpiration.getTime() - System.currentTimeMillis();
                                if (refreshTimeLeft < refreshTokenExpiryMs * 0.8) {
                                    String newRefreshToken = jwtUtil.generateToken(username, refreshTokenExpiryMs);
                                    Cookie newRefreshCookie = new Cookie("Refresh-token", newRefreshToken);
                                    newRefreshCookie.setHttpOnly(true);
                                    newRefreshCookie.setSecure(true);
                                    newRefreshCookie.setPath("/");
                                    response.addCookie(newRefreshCookie);
                                    log.info("Rotated both tokens for user: {}", username);
                                } else {
                                    log.info("Refreshed access token only for user: {}", username);
                                }
                            }
                        } catch (ExpiredJwtException e) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Refresh token expired");
                            return;
                        }
                        break;
                    }
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}