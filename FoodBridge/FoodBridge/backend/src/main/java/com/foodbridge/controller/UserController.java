package com.foodbridge.controller;

import com.foodbridge.dto.UserDtos;
import com.foodbridge.entity.User;
import com.foodbridge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDtos.UserResponse>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/donors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDtos.UserResponse>> getAllDonors(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/receivers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDtos.UserResponse>> getAllReceivers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication.principal)")
    public ResponseEntity<UserDtos.UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication.principal)")
    public ResponseEntity<UserDtos.UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUser(id, request, currentUser));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtos.UserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(id, request));
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id, authentication.principal)")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserDtos.PasswordChangeRequest request,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(id, request, currentUser);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDtos.UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDtos.UserResponse.from(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDtos.UserResponse> updateCurrentUser(
            @Valid @RequestBody UserDtos.UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), request, currentUser));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            @Valid @RequestBody UserDtos.PasswordChangeRequest request,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(currentUser.getId(), request, currentUser);
        return ResponseEntity.ok().build();
    }
}