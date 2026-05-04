package com.example.food_mart.config;

import com.example.food_mart.common.argumentResolver.StaffInfoArgumentResolver;
import com.example.food_mart.common.argumentResolver.UserInfoArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");

        String absoluteUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString(); // file:///C:/devJava/food-mart/uploads/items 과 같은 형식으로 만드는거
        registry.addResourceHandler("/uploads/items/**")
                .addResourceLocations(absoluteUploadPath + "/");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserInfoArgumentResolver());
        resolvers.add(new StaffInfoArgumentResolver());
    }
}
