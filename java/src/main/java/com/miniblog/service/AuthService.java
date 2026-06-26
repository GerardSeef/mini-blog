package com.miniblog.service;

import com.miniblog.dto.AuthRequest;
import com.miniblog.dto.AuthResponse;
import com.miniblog.dto.LoginRequest;
import com.miniblog.dto.UserResponse;
import com.miniblog.entity.User;
import com.miniblog.repository.UserRepository;
import com.miniblog.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(AuthRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getId(), user.getEmail());
        return AuthResponse.builder()
            .token(token)
            .user(mapToUserResponse(user))
            .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();

        String token = jwtProvider.generateToken(user.getId(), user.getEmail());
        return AuthResponse.builder()
            .token(token)
            .user(mapToUserResponse(user))
            .build();
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow();
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
