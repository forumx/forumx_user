package com.example.forumx_user.config.security;

import com.example.forumx_user.config.security.filter.CustomAuthorizationRedirectFilter;
import com.example.forumx_user.config.security.filter.TokenFilter;
import com.example.forumx_user.config.security.oauth2.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.auth0.issuerUri}")
    private String issuer;
    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    private final UserDetailsService userDetailsService;

    private final TokenFilter tokenFilter;

    private final CustomAuthorizedClientService customAuthorizedClientService;

    private final CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository;

    private final CustomAuthorizationRedirectFilter customAuthorizationRedirectFilter;
    private final OAuthController oAuthController;

    private final UnauthenticatedRequestHandler unauthenticatedRequestHandler;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          TokenFilter tokenFilter,
                          CustomAuthorizationRedirectFilter customAuthorizationRedirectFilter,
                          CustomAuthorizedClientService customAuthorizedClientService,
                          CustomStatelessAuthorizationRequestRepository customStatelessAuthorizationRequestRepository,
                          OAuthController oAuthController,
                          UnauthenticatedRequestHandler unauthenticatedRequestHandler)
    {
        this.userDetailsService = userDetailsService;
        this.tokenFilter = tokenFilter;
        this.customAuthorizationRedirectFilter = customAuthorizationRedirectFilter;
        this.customStatelessAuthorizationRequestRepository = customStatelessAuthorizationRequestRepository;
        this.customAuthorizedClientService = customAuthorizedClientService;
        this.oAuthController = oAuthController;
        this.unauthenticatedRequestHandler = unauthenticatedRequestHandler;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
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
                        subconfig.authorizationRequestRepository(this.customStatelessAuthorizationRequestRepository);
                    });
                    config.redirectionEndpoint(subconfig -> {
                        subconfig.baseUri(OAuthController.CALLBACK_BASE_URL + "/*");
                    });
                    config.authorizedClientService(this.customAuthorizedClientService);
                    config.successHandler(oAuthController::oauthSuccessResponse);
                    config.failureHandler(oAuthController::oauthFailureResponse);
                })
                .logout(logout -> logout.logoutSuccessHandler(logoutHandler())
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")))
                // Filters
                .addFilterBefore(this.customAuthorizationRedirectFilter, OAuth2AuthorizationRequestRedirectFilter.class);
        // Auth exceptions
//                .exceptionHandling(config -> {
//                    config.authenticationEntryPoint(unauthenticatedRequestHandler);
//                });
        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }

    private LogoutSuccessHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
//                response.sendRedirect(issuer + "v2/logout?client_id=" + clientId);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.getWriter().write("{ \"redirectUrl\": \"%s\" }".formatted(issuer + "v2/logout?client_id=" + clientId));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
