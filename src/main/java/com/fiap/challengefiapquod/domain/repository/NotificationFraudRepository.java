package com.fiap.challengefiapquod.domain.repository;

import com.fiap.challengefiapquod.domain.model.NotificacaoFraude;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationFraudRepository extends MongoRepository<NotificacaoFraude, String> {
    NotificacaoFraude findByTransacaoId(String transacaoId);
}