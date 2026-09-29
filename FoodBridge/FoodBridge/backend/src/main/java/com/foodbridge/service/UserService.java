package com.foodbridge.service;

import com.foodbridge.dto.UserDtos;
import com.foodbridge.entity.User;
import com.foodbridge.exception.BadRequestException;
import com.foodbridge.exception.ResourceNotFoundException;
import com.foodbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDtos.UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDtos.UserResponse::from);
    }

    public List<UserDtos.UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserDtos.UserResponse::from).toList();
    }

    public List<UserDtos.UserResponse> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream().map(UserDtos.UserResponse::from).toList();
    }

    public UserDtos.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDtos.UserResponse.from(user);
    }

    @Transactional
    public UserDtos.UserResponse updateUser(Long id, UserDtos.UserUpdateRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own profile");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return UserDtos.UserResponse.from(user);
    }

    @Transactional
    public UserDtos.UserResponse updateUserStatus(Long id, UserDtos.UserStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(request.getStatus());
        user = userRepository.save(user);
        return UserDtos.UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long id, UserDtos.PasswordChangeRequest request, User currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only change your own password");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    public long countByRoleAndStatus(User.Role role, User.UserStatus status) {
        return userRepository.countByRoleAndStatus(role, status);
    }
}