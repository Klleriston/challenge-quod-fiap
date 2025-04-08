package com.fiap.challengefiapquod.presentation.controller;

import com.fiap.challengefiapquod.application.dto.AuthRequestDTO;
import com.fiap.challengefiapquod.application.dto.AuthResponseDTO;
import com.fiap.challengefiapquod.application.dto.UserDTO;
import com.fiap.challengefiapquod.domain.model.User;
import com.fiap.challengefiapquod.domain.service.UserService;
import com.fiap.challengefiapquod.infrastructure.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userService.save(user);
        userDto.setId(savedUser.getId());
        userDto.setPassword(null);
        return ResponseEntity.status(201).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        User user = userService.findByEmail(authRequest.getEmail());
        if (user == null || !passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtTokenProvider.generateToken(user.getEmail());
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        return ResponseEntity.ok(response);
    }
}