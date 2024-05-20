package com.example.forumx_user.config.security.oauth2;

import com.example.forumx_user.config.security.helpers.AuthenticationHelper;
import com.example.forumx_user.config.security.helpers.CookieHelper;
import com.example.forumx_user.service.AccountService;
import com.example.forumx_user.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.UUID;

@Controller
public class OAuthController {
    public static final String AUTHORIZATION_BASE_URL = "/oauth2/authorization";

    public static final String CALLBACK_BASE_URL = "/login/oauth2/code";

    public static final String OAUTH_COOKIE_NAME = "OAUTH";
    public static final String SESSION_COOKIE_NAME = "SESSION";

    private final AccountService accountService;

    private final TokenService tokenService;



    @Value("${frontend.domain:localhost}")
    private String domain;


    @Autowired
    public OAuthController(AccountService accountService, TokenService tokenService){
        this.accountService = accountService;
        this.tokenService = tokenService;
    }


    @SneakyThrows
    public void oauthRedirectResponse(HttpServletRequest request, HttpServletResponse response, String url) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{ \"redirectUrl\": \"%s\" }".formatted(url));
    }

    @SneakyThrows
    public void oauthSuccessCallback(OAuth2AuthorizedClient client, Authentication authentication) {
        // You can grab the access + refresh tokens as well via the "client"
         Long userId = this.accountService.findOrRegisterAccount(
                authentication.getName(),
                authentication.getName().split("\\|")[0],
                ((DefaultOidcUser) authentication.getPrincipal()).getClaims()
        );
        AuthenticationHelper.attachUserId(authentication, userId);
    }

    @SneakyThrows
    public void oauthSuccessResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Long userId = AuthenticationHelper.retrieveUserId(authentication);
        response.addHeader(HttpHeaders.SET_COOKIE, CookieHelper.generateExpiredCookie(OAUTH_COOKIE_NAME, request));
//        response.addHeader(HttpHeaders.SET_COOKIE, CookieHelper.generateCookie(SESSION_COOKIE_NAME, accountId, Duration.ofDays(1), request));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        response.addHeader("Set-Cookie","AUTH_TOKEN="+ tokenService.createTokenFromUserId(userId) +"; Domain = "+domain+"; Path=/; HttpOnly");
        response.getWriter().write("{ \"status\": \"success\" }");
    }

    @SneakyThrows
    public void oauthFailureResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.SET_COOKIE, CookieHelper.generateExpiredCookie(OAUTH_COOKIE_NAME, request));
        response.getWriter().write("{ \"status\": \"failure\" }");
    }
}