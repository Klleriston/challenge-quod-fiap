package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}