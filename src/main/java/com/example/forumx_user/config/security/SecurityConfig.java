package com.example.forumx_user.config.security;

import com.example.forumx_user.config.security.filter.CustomAuthorizationRedirectFilter;
import com.example.forumx_user.config.security.filter.TokenFilter;
import com.example.forumx_user.config.security.oauth2.CustomAuthorizationRequestResolver;
import com.example.forumx_user.config.security.oauth2.CustomAuthorizedClientService;
import com.example.forumx_user.config.security.oauth2.CustomStatelessAuthorizationRequestRepository;
import com.example.forumx_user.config.security.oauth2.OAuthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    private final TokenFilter tokenFilter;

    private final CustomAuthorizedClientService customAuthorizedClientService;

    private final CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository;

    private final CustomAuthorizationRedirectFilter customAuthorizationRedirectFilter;

    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    private final OAuthController oAuthController;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          TokenFilter tokenFilter,
                          CustomAuthorizationRedirectFilter customAuthorizationRedirectFilter,
                          CustomAuthorizedClientService customAuthorizedClientService,
                          CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository,
                          CustomAuthorizationRequestResolver customAuthorizationRequestResolver,
                          OAuthController oAuthController
                          ){
        this.userDetailsService = userDetailsService;
        this.tokenFilter = tokenFilter;
        this.customAuthorizationRedirectFilter = customAuthorizationRedirectFilter;
        this.customStatelessAuthorizationRequestRepository = customStatelessAuthorizationRequestRepository;
        this.customAuthorizedClientService = customAuthorizedClientService;
        this.customAuthorizationRequestResolver = customAuthorizationRequestResolver;
        this.oAuthController = oAuthController;
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/user/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(config -> {
                    config.authorizationEndpoint(subconfig -> {
                        subconfig.baseUri(OAuthController.AUTHORIZATION_BASE_URL);
                        subconfig.authorizationRequestResolver(this.customAuthorizationRequestResolver);
                        subconfig.authorizationRequestRepository(this.customStatelessAuthorizationRequestRepository);
                    });
                    config.redirectionEndpoint(subconfig -> {
                        subconfig.baseUri(OAuthController.CALLBACK_BASE_URL + "/*");
                    });
                    config.authorizedClientService(this.customAuthorizedClientService);
                    config.successHandler(oAuthController::oauthSuccessResponse);
                    config.failureHandler(oAuthController::oauthFailureResponse);
                })
                // Filters
                .addFilterBefore(this.customAuthorizationRedirectFilter, OAuth2AuthorizationRequestRedirectFilter.class);
                // Auth exceptions
//                .exceptionHandling(config -> {
//                    config.accessDeniedHandler(this::accessDenied);
//                    config.authenticationEntryPoint(this::accessDenied);
//                });
//        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

}
