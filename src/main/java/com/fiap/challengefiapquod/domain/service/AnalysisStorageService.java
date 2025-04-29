package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.application.dto.FraudAnalysisResultDTO;
import com.fiap.challengefiapquod.domain.model.AnalysisRecord;
import com.fiap.challengefiapquod.domain.repository.AnalysisRecordRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AnalysisStorageService {

    private final AnalysisRecordRepository analysisRecordRepository;

    public AnalysisStorageService(AnalysisRecordRepository analysisRecordRepository) {
        this.analysisRecordRepository = analysisRecordRepository;
    }

    public AnalysisRecord storeAnalysisResult(String userId, String imageUrl, FraudAnalysisResultDTO analysisResult) {
        AnalysisRecord record = new AnalysisRecord();
        record.setUserId(userId);
        record.setImageUrl(imageUrl);
        record.setAnalysisId(analysisResult.getAnalysisId());
        record.setAnalysisDate(new Date());
        record.setFraud(analysisResult.isFraud());
        record.setFraudReason(analysisResult.getFraudReason());
        record.setImageDescription(analysisResult.getImageDescription());
        record.setContainsFaces(analysisResult.isContainsFaces());
        record.setFaceCount(analysisResult.getFaceCount());

        return analysisRecordRepository.save(record);
    }

    public List<AnalysisRecord> getUserAnalysisHistory(String userId) {
        return analysisRecordRepository.findByUserIdOrderByAnalysisDateDesc(userId);
    }

    public AnalysisRecord getAnalysisRecord(String analysisId) {
        return analysisRecordRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Análise não encontrada: " + analysisId));
    }
}