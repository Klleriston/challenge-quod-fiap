package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.AnalysisRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnalysisRecordRepository extends MongoRepository<AnalysisRecord, String> {
    List<AnalysisRecord> findByUserIdOrderByAnalysisDateDesc(String userId);
    List<AnalysisRecord> findByIsFraudIsTrue();
}