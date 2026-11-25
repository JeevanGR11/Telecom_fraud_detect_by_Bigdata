package com.telecom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/dashboard.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/register").setViewName("forward:/register.html");
        registry.addViewController("/dashboard").setViewName("forward:/dashboard.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File frontendDir = new File("frontend");
        String frontendPath = "file:" + frontendDir.getAbsolutePath().replace("\\", "/") + "/";

        registry.addResourceHandler("/**")
                .addResourceLocations(frontendPath, "classpath:/static/")
                .setCachePeriod(0);
    }
}
