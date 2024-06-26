package com.example.forumx_user.service;

import com.example.forumx_user.entity.UserEntity;
import com.example.forumx_user.exception.NotFoundException;
import com.example.forumx_user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TokenService {

    @Value("${jwt.secretkey:forumx}")
    private String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private UserRepository userRepository;

    public String createTokenFromUserName(String username){
        UserEntity userEntity = userRepository.findByUsername(username);
        if(userEntity!=null){
            return createToken(userEntity);
        }else{
            throw new NotFoundException("Not found user");
        }
    }

    public String createTokenFromUserId(Long userId){
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if(userEntity!=null){
            return createToken(userEntity);
        }else{
            throw new NotFoundException("Not found user");
        }
    }
    public String createToken(UserEntity userEntity) {

        log.info(userEntity.getUsername());
        return Jwts.builder()
                .setSubject(userEntity.getEmail())
                .claim("ROLE", userEntity.getRole())
                .claim("NAME", userEntity.getName())
                .claim("PICTURE", userEntity.getImg_url())
                .setIssuedAt(new Date())
                .setExpiration(expireDate())
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

    public static Date expireDate(){
        long validity = 10;
        Date now = new Date();
        return new Date(now.getTime() + validity * 60 * 1000);

    }

}
