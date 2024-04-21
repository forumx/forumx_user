package com.example.forumx_user.config.security;

import com.example.forumx_user.config.security.filter.TokenFilter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;

    private final TokenFilter tokenFilter;

    private String AUTHORIZATION_ENDPOINT = "/oauth2/authorize";

    private String REDIRECTION_ENDPOINT = "/oauth2/callback/*";

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService,
                          TokenFilter tokenFilter){
        this.userDetailsService = userDetailsService;
        this.tokenFilter = tokenFilter;
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth)-> auth
                        .requestMatchers("/api/user/**").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyAuthority( "ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/oauth/login**").permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
//                .oauth2Login(config -> {
//                    config.authorizationEndpoint(
//                            authorizationEndpointConfig -> {
//                                authorizationEndpointConfig.baseUri(AUTHORIZATION_ENDPOINT);
//                                authorizationEndpointConfig.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository);
//                            }
//                    );
//                    config.redirectionEndpoint(redirectionEndpointConfig -> {
//                        redirectionEndpointConfig.baseUri(REDIRECTION_ENDPOINT);
//                    });
//                    config.userInfoEndpoint(userInfoEndpointConfig -> {
//                        userInfoEndpointConfig.userService(customOAuth2UserService);
//                    });
//                    config.successHandler(oAuth2AuthenticationSuccessHandler);
//                    config.failureHandler(oAuth2AuthenticationFailureHandler);
//                });
        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

}
