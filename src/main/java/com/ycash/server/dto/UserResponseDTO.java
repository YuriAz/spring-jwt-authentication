package com.ycash.server.dto;

import com.ycash.server.model.UserModel;

import java.time.LocalDateTime;

public record UserResponseDTO(
        String id,
        String firstName,
        String lastName,
        String email,
        String password,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserResponseDTO(UserModel userModel) {
        this(
                userModel.getId(),
                userModel.getFirstName(),
                userModel.getLastName(),
                userModel.getEmail(),
                userModel.getPassword(),
                userModel.getRole().name(),
                userModel.getCreatedAt(),
                userModel.getUpdatedAt()
        );
    }
}
