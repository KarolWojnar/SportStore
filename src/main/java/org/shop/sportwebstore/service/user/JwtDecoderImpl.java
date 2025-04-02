package org.shop.sportwebstore.service.user;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@RequiredArgsConstructor
@Slf4j
public class JwtDecoderImpl implements JwtDecoder {

    private final JwtService jwtService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (jwtService.isBlackListed(token)) {
                throw new JwtException("Token is blacklisted");
            }
            Claims claims = jwtService.extractAllClaims(token);
            return Jwt.withTokenValue(token)
                    .header("alg", "HS256")
                    .header("typ", "JWT")
                    .subject(claims.getSubject())
                    .issuedAt(claims.getIssuedAt().toInstant())
                    .expiresAt(claims.getExpiration().toInstant())
                    .claim("role", claims.get("role"))

                    .build();
        } catch (Exception e) {
            throw new JwtException("Invalid token", e);
        }
    }
}