package com.rc.readcompass.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.attachment-path}")
    private String attachmentPath;

    @Value("${app.storage.attachment-url-path}")
    private String attachmentUrlPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(attachmentUrlPath + "/**")
                .addResourceLocations("file:" + attachmentPath + "/");
    }
}
