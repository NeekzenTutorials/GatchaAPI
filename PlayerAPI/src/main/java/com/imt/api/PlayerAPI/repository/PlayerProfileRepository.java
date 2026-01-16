package com.imt.api.PlayerAPI.repository;

import com.imt.api.PlayerAPI.model.PlayerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlayerProfileRepository extends MongoRepository<PlayerProfile, String> {
  boolean existsByPseudo(String pseudo);
  Optional<PlayerProfile> findByPseudo(String pseudo);
}
