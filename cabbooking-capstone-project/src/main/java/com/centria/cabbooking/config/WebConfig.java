package com.centria.cabbooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The frontend is served from the same Spring Boot instance
 * (src/main/resources/static/frontend), so CORS is not strictly required
 * for the default setup - but it's enabled permissively here in case the
 * frontend is ever hosted separately (e.g. opened directly as a local
 * file, or served via a different dev port).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
