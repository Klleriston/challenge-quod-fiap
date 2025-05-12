package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
}