package com.imt.api.InvocationAPI.repository;

import com.imt.api.InvocationAPI.model.MonsterTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MonsterTemplateRepository extends MongoRepository<MonsterTemplate, String> {
  Optional<MonsterTemplate> findByName(String name);
  boolean existsByName(String name);
}
