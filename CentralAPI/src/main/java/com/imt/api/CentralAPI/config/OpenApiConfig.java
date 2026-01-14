package com.imt.api.CentralAPI.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI centralOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("CentralAPI (BFF) - Documentation")
            .description("Point d'entrée unique pour le front. CentralAPI appelle les microservices internes (AuthAPI, etc.).")
            .version("0.0.1"));
  }
}
