package com.fiap.challengefiapquod.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "analysis_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecord {
    @Id
    private String analysisId;
    private String userId;
    private String imageUrl;
    private Date analysisDate;
    private boolean isFraud;
    private String fraudReason;
    private String imageDescription;
    private boolean containsFaces;
    private int faceCount;
}