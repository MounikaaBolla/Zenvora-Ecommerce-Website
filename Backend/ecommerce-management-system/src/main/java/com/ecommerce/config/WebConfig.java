package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String imagePath = Paths
                .get(
                        System.getProperty("user.dir"),
                        "src",
                        "main",
                        "resources",
                        "static",
                        "images",
                        "products"
                )
                .toAbsolutePath()
                .toUri()
                .toString();

        registry
                .addResourceHandler("/images/products/**")
                .addResourceLocations(imagePath);
    }
}