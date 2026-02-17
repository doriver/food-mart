package com.example.food_mart.config;

import com.example.food_mart.common.argumentResolver.StaffInfoArgumentResolver;
import com.example.food_mart.common.argumentResolver.UserInfoArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico") // [http://localhost:8080/favicon.ico](http://localhost:8080/favicon.ico)
                .addResourceLocations("classpath:/static/"); // 브라우저 탭에 표시되는 아이콘
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserInfoArgumentResolver());
        resolvers.add(new StaffInfoArgumentResolver());
    }
}
