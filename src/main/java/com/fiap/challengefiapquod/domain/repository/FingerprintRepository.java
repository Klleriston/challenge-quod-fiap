package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.FingerprintRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FingerprintRepository extends MongoRepository<FingerprintRecord, String> {
    Optional<FingerprintRecord> findByUserId(String userId);
    Optional<FingerprintRecord> findByImageUrl(String imageUrl);
    boolean existsByImageUrl(String imageUrl);
}