//src/main/java/com/cloudflix/backend/config/WebMvcConfig.java
/*package com.cloudflix.backend.config;

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
*/

// src/main/java/com/cloudflix/backend/config/WebMvcConfig.java
package com.cloudflix.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper; // <<< ADD THIS IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // <<< ADD THIS IMPORT
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    // Inject the ObjectMapper bean that you configured in JacksonConfig.java
    @Autowired
    private ObjectMapper objectMapper; // <<< INJECT YOUR CUSTOM OBJECTMAPPER

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.info("Configuring HttpMessageConverters...");

        // Remove any existing Jackson converters to avoid duplicates or conflicts
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);

        // Create a new Jackson converter and set YOUR custom ObjectMapper on it
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper); // <<< USE YOUR CUSTOM OBJECTMAPPER

        // Add your configured Jackson converter, preferably at the beginning
        converters.add(0, jacksonConverter);

        logger.info("HttpMessageConverters after custom Jackson configuration: {}", converters.size());
        converters.forEach(c -> logger.info("Converter: {}", c.getClass().getName()));
    }
}