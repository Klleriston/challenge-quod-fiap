package com.fiap.challengefiapquod.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "fingerprint_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintRecord {
    @Id
    private String id;
    private String userId;
    private String imageUrl;
    private String fingerprintHash;
    private Date registrationDate;
}