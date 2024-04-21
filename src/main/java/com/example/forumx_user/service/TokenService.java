package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TokenService {

    @Value("${jwt.secretkey:forumx}")
    private String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public String createToken(UserEntity userEntity) {
        long validity = 10;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity * 60 * 1000);
        log.info(userEntity.getUsername());
        return Jwts.builder()
                .setSubject(userEntity.getEmail())
                .claim("ROLE", userEntity.getRole())
                .claim("NAME", userEntity.getName())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String getUsername(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return null;
    }


}
