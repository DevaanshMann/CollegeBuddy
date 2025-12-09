package com.collegebuddy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${collegebuddy.storage.local.upload-dir}")
    private String uploadDir;

    @Value("${collegebuddy.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:5174}")
    private String allowedOrigins;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = uploadDir.startsWith("/") ? "file:" + uploadDir : "file:" + uploadDir;
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);

        log.info("Configured resource handler for /uploads/avatars/ with location: {}", resourceLocation);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] originsArray = allowedOrigins.split(",");
        registry.addMapping("/uploads/**")
                .allowedOrigins(originsArray)
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowCredentials(false);
    }
}