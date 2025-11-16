package com.fittrack.mainapp.config;

import com.fittrack.mainapp.interceptor.UserActivityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserActivityInterceptor userActivityInterceptor;

    public WebConfig(UserActivityInterceptor userActivityInterceptor) {
        this.userActivityInterceptor = userActivityInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActivityInterceptor);
    }
}