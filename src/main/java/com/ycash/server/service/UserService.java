package com.ycash.server.service;

import com.ycash.server.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface UserService {

    AuthenticationResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO);

    AuthenticationResponseDTO authenticate(AuthenticationRequestDTO authenticationRequest);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    AuthenticationResponseDTO verifyCode(VerificationRequestDTO verificationRequestDTO);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(String id);

    Boolean updateUser(UserRegisterRequestDTO userRegisterRequestDTO, String id);

    Boolean deleteUser(String id);
}
