package com.imt.api.MonsterAPI.repository;

import com.imt.api.MonsterAPI.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MonsterRepository extends MongoRepository<Monster, String> {
  List<Monster> findByOwnerPseudo(String ownerPseudo);
  Optional<Monster> findByIdAndOwnerPseudo(String id, String ownerPseudo);
  void deleteByIdAndOwnerPseudo(String id, String ownerPseudo);
}
