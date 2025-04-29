package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImageRepository extends MongoRepository<Image, String> {
    List<Image> findByUserId(String userId);
}