package com.openstudy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 *
 * @author openstudy
 */
@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class OpenStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenStudyApplication.class, args);
        log.info(
                "\n" +
                        "╔══════════════════════════════════════════════╗\n" +
                        "║                                              ║\n" +
                        "║      (♥◠‿◠)ﾉﾞ  OpenStudy 启动成功  ﾍ(´ڡ`ﾍ)  ║\n" +
                        "║                                              ║\n" +
                        "║     ✨ AI刷题 · Markdown笔记 · 开放学习 ✨    ║\n" +
                        "║                                              ║\n" +
                        "╚══════════════════════════════════════════════╝\n" +
                        "                (づ｡◕‿‿◕｡)づ");
    }
}