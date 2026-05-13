// package com.openstudy.framework.config;
//
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
// @Configuration
// public class ResourcesConfig implements WebMvcConfigurer {
//
//     @Override
//     public void addResourceHandlers(ResourceHandlerRegistry registry) {
//         // 映射 /upload/ocr/** 到本地 upload/ocr 目录
//         String projectPath = System.getProperty("user.dir");
//         registry.addResourceHandler("/upload/ocr/**")
//                 .addResourceLocations("file:" + projectPath + "/upload/ocr/");
//     }
// }