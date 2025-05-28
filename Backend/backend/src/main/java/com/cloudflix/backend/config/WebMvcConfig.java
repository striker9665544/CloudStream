//src/main/java/com/cloudflix/backend/config/WebMvcConfig.java
package com.cloudflix.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.info("Default HttpMessageConverters before modification: {}", converters.size());
        for (int i = 0; i < converters.size(); i++) {
            logger.info("Converter [{}]: {}", i, converters.get(i).getClass().getName());
        }

        // Add Jackson2 message converter as the first one
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        converters.add(0, jacksonConverter);
        // You could also try clearing and adding only Jackson:
        // converters.clear();
        // converters.add(jacksonConverter);


        logger.info("HttpMessageConverters after adding Jackson explicitly: {}", converters.size());
        for (int i = 0; i < converters.size(); i++) {
            logger.info("Converter [{}]: {}", i, converters.get(i).getClass().getName());
        }
    }
}