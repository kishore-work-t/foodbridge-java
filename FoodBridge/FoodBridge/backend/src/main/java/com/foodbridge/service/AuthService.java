package com.foodbridge.service;

import com.foodbridge.dto.AuthDtos;
import com.foodbridge.dto.UserDtos;
import com.foodbridge.entity.User;
import com.foodbridge.entity.Organization;
import com.foodbridge.exception.BadRequestException;
import com.foodbridge.exception.DuplicateResourceException;
import com.foodbridge.repository.UserRepository;
import com.foodbridge.repository.OrganizationRepository;
import com.foodbridge.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        if (request.getRole() == User.Role.RECEIVER) {
            Organization organization = Organization.builder()
                    .user(user)
                    .verificationStatus(Organization.VerificationStatus.PENDING)
                    .build();
            organizationRepository.save(organization);
        }

        String jwtToken = jwtService.generateToken(user);
        return new AuthDtos.AuthResponse(jwtToken, user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        String jwtToken = jwtService.generateToken(user);
        return new AuthDtos.AuthResponse(jwtToken, user);
    }

    public UserDtos.UserResponse getCurrentUser(User user) {
        return UserDtos.UserResponse.from(user);
    }
}