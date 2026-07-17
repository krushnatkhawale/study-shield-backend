package com.studyshield.content.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studyShieldOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Study Shield - Content Service API")
                        .version("1.0.0")
                        .description("Central service for educational content management")
                        .contact(new Contact()
                                .name("Study Shield Team")));
    }
}
