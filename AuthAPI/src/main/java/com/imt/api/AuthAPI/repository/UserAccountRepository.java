package com.imt.api.AuthAPI.repository;

import com.imt.api.AuthAPI.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
  Optional<UserAccount> findByEmail(String email);
  boolean existsByEmail(String email);
}
