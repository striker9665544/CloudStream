package com.cloudflix.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // Ensures this ObjectMapper is used by default by Spring MVC
    public ObjectMapper objectMapper() {
    	System.out.println("****** JacksonConfig: Creating primary ObjectMapper bean! ******");
        ObjectMapper mapper = new ObjectMapper();

        // Register the JavaTimeModule to handle Java 8 Date & Time API types (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Crucial: Disable writing dates as numeric timestamps (or arrays for LocalDateTime).
        // This forces Jackson to use its default ISO-8601 string format for java.time types.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Optional: Configure other Jackson features if needed
        // mapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing JSON responses (dev only)
        // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Don't include null fields

        return mapper;
    }
}