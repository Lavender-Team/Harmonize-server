package kr.ac.chungbuk.harmonize.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Date;

public class JwtTokenProvider {
    private static final String JWT_SECRET = "woievndjkwpodwlkfjfioehoifjwqwpqewefkjwelfjwllkjlwecwoievndjkwpodwlkfjfioehoifjwqwpqewefkjwelfjwllkjlwec";
    private static final Key JWT_SECRET_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    // 토큰 유효시간
    private static final int JWT_EXPIRATION_MS = 168 * 60 * 60 * 1000;

    private static JwtParser jwtParser;

    static {
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(JWT_SECRET_KEY)
                .build();
    }

    // 토큰 생성
    public static String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        return Jwts.builder()
                .setSubject((String) authentication.getPrincipal())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(JWT_SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // 토큰에서 username 추출
    public static String getSidFromJwt(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // JWT 토큰 유효성 검사
    public static boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            //log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            //log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            //log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            //log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            //log.error("JWT claims string is empty.");
        }
        return false;
    }
}
