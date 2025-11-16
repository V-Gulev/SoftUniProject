package com.fittrack.mainapp.interceptor;

import com.fittrack.mainapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public UserActivityInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                user.setLastActivity(LocalDateTime.now());
                userRepository.save(user);
            });
        }
        return true;
    }
}