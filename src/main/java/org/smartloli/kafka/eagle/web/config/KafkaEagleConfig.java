package org.smartloli.kafka.eagle.web.config;

import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.plugin.font.FigletFont;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * @author zhiwei_yang
 * @time 2020-6-30-10:03
 */
@Configuration
@Slf4j
public class KafkaEagleConfig {

    /**
     * 打印提示语
     *
     * @throws IOException
     */
    @PostConstruct
    public void welcome() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String name = FigletFont.class.getClassLoader().getResource("").getPath() + "font/slant.flf";
        File file = new File(name);
        String asciiArt = FigletFont.convertOneLine(file, "KAfKA EAGLE");
        stringBuilder.append("Welcome to").append("\r\n");
        stringBuilder.append(asciiArt).append("\r\n");
        stringBuilder.append("Version 1.4.6 -- Copyright 2016-2020");
        log.info(stringBuilder.toString());
    }
}
