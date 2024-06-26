package com.example.forumx_user.config.security.filter;

import com.example.forumx_user.config.security.oauth2.OAuthController;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class CustomAuthorizationRedirectFilter extends OAuth2AuthorizationRequestRedirectFilter {

    @SneakyThrows
    @Autowired
    public CustomAuthorizationRedirectFilter(
            OAuthController oAuthController,
            OAuth2AuthorizationRequestResolver authorizationRequestResolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository
    ) {
        super(authorizationRequestResolver);
        super.setAuthorizationRequestRepository(authorizationRequestRepository);
        // Reflection hack to overwrite the parent's redirect strategy
        RedirectStrategy customStrategy = oAuthController::oauthRedirectResponse;
        Field field = OAuth2AuthorizationRequestRedirectFilter.class.getDeclaredField("authorizationRedirectStrategy");
        field.setAccessible(true);
        field.set(this, customStrategy);
    }

}