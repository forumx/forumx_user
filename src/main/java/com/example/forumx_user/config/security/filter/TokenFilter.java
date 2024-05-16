package com.example.forumx_user.config.security.filter;

import com.example.forumx_user.config.security.oauth2.UnauthenticatedRequestHandler;
import com.example.forumx_user.service.TokenService;
import com.example.forumx_user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class TokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    private final UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    @Autowired
    public TokenFilter(TokenService tokenService, UserDetailsService userDetailsService, UnauthenticatedRequestHandler unauthenticatedRequestHandler){
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.unauthenticatedRequestHandler = unauthenticatedRequestHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getServletPath().startsWith("/api/")) {
            filterChain.doFilter(request,response);
        }else {
            try {
                String jwt = getJwtFromRequest(request);
                if (StringUtils.hasText(jwt)) {
                    String username = tokenService.getUsername(jwt);
                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (userDetails == null) {
                            throw new BadCredentialsException("Invalid jwt");
                        }
                        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        throw new BadCredentialsException("Invalid jwt");
                    }
                } else {
                    throw new BadCredentialsException("Don't have jwt");
                }
                filterChain.doFilter(request, response);
            } catch (AuthenticationException ex) {
                unauthenticatedRequestHandler.commence(request, response, ex);
            }
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie authCookie = cookies == null ? null : Arrays.stream(cookies).
                filter(cookie -> cookie.getName().equals("AUTH_TOKEN"))
                .findAny().orElse(null);
        if(authCookie!=null) {
            String bearerToken = authCookie.getValue();
            log.info(bearerToken);
            return bearerToken;
        }
        return null;
    }
}
