package com.fittrack.mainapp.config;

import com.fittrack.mainapp.filter.AuthenticatedUserRedirectFilter;
import com.fittrack.mainapp.service.impl.CustomOAuth2UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final AuthenticatedUserRedirectFilter authenticatedUserRedirectFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomAuthenticationFailureHandler authenticationFailureHandler,
                          AuthenticatedUserRedirectFilter authenticatedUserRedirectFilter,
                          CustomOAuth2UserService customOAuth2UserService) {
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authenticatedUserRedirectFilter = authenticatedUserRedirectFilter;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(authenticatedUserRedirectFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/", "/login", "/register", "/login-error", "/blocked")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureHandler(authenticationFailureHandler))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .defaultSuccessUrl("/dashboard", true))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true));

        return http.build();
    }
}