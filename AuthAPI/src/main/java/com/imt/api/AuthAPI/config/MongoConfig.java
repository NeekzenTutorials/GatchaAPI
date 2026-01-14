package com.imt.api.AuthAPI.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class MongoConfig {

  @Bean
  @Primary
  public MongoClientSettings mongoClientSettings(@Value("${spring.data.mongodb.uri}") String uri) {
    return MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(uri))
        .build();
  }

  @Bean
  @Primary
  public MongoClient mongoClient(MongoClientSettings settings) {
    return MongoClients.create(settings);
  }
}
