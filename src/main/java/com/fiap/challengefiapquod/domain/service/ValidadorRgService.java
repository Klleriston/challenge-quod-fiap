package com.fiap.challengefiapquod.domain.service;

import org.springframework.stereotype.Service;

@Service
public class ValidadorRgService {

    public boolean validarRg(String rg) {
        rg = rg.replaceAll("[^0-9A-Za-z]", "");

        if (rg.length() < 8 || rg.length() > 10) {
            return false;
        }

        return true;
    }
}