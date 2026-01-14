package com.imt.api.AuthAPI.repository;

import com.imt.api.AuthAPI.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
  boolean existsByEmail(String email);
  boolean existsByPseudo(String pseudo);
  Optional<UserAccount> findByEmail(String email);
  Optional<UserAccount> findByPseudo(String pseudo);
}