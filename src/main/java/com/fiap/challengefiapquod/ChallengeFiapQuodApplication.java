package com.fiap.challengefiapquod;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Base64;

@SpringBootApplication
public class ChallengeFiapQuodApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallengeFiapQuodApplication.class, args);
    }

}
