package com.fiap.challengefiapquod.domain.service;

import com.fiap.challengefiapquod.domain.model.User;
import com.fiap.challengefiapquod.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User with id " + user.getId() + " not found"));

        User userWithEmail = userRepository.findByEmail(user.getEmail());
        if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
            throw new IllegalStateException("Email already in use");
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());

        return userRepository.save(existingUser);
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}