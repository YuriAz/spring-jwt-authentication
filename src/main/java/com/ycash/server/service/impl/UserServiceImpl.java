package com.ycash.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycash.server.dto.*;
import com.ycash.server.enums.TokenType;
import com.ycash.server.model.Token;
import com.ycash.server.model.UserModel;
import com.ycash.server.repository.TokenRepository;
import com.ycash.server.repository.UserRepository;
import com.ycash.server.service.JwtService;
import com.ycash.server.service.TwoFactorAuthenticationService;
import com.ycash.server.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @Override
    @Transactional
    public AuthenticationResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        UserModel userModel = new UserModel(userRegisterRequestDTO);
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));

        if (userRegisterRequestDTO.mfaEnabled()) {
            userModel.setSecret(twoFactorAuthenticationService.generateNewSecret());
        }

        UserModel savedUser = userRepository.save(userModel);
        String jwtToken = jwtService.generateToken(userModel);
        String refreshToken = jwtService.generateRefreshToken(userModel);
        saveUserToken(savedUser, jwtToken);
        return new AuthenticationResponseDTO(
                jwtToken,
                refreshToken,
                userModel.isMfaEnabled(),
                twoFactorAuthenticationService.generateQrCodeImageUri(userModel.getSecret())
        );
    }

    @Override
    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.email(),
                        authenticationRequest.password()
                )
        );

        Optional<UserModel> userModel = userRepository.findUserByEmail(authenticationRequest.email());

        if (userModel.isEmpty()) {
            return null;
        }

        if (userModel.get().isMfaEnabled()) {
            return new AuthenticationResponseDTO("", "", true, "");
        }

        String token = jwtService.generateToken(userModel.get());
        String refreshToken = jwtService.generateRefreshToken(userModel.get());
        revokeAllUserTokens(userModel.get());
        saveUserToken(userModel.get(), token);
        return new AuthenticationResponseDTO(token, refreshToken, false, "");
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            UserModel user = userRepository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                AuthenticationResponseDTO authResponse = new AuthenticationResponseDTO(accessToken, refreshToken, false, "");
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    public AuthenticationResponseDTO verifyCode(VerificationRequestDTO verificationRequestDTO) {
        UserModel userModel = userRepository.findUserByEmail(verificationRequestDTO.email())
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format("No user found with %S", verificationRequestDTO.email()))
                );

        if (twoFactorAuthenticationService.isOtpNotValid(userModel.getSecret(), verificationRequestDTO.code())) {
            throw new BadCredentialsException("Code is not correct.");
        }

        String jwtToken = jwtService.generateToken(userModel);
        return new AuthenticationResponseDTO(jwtToken, "", userModel.isMfaEnabled(), "");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        List<UserModel> usersList = userRepository.findAll();
        return usersList.stream().map(this::userModelToUserResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String id) {
        Optional<UserModel> userModelOptional = userRepository.findById(id);
        return userModelOptional.isEmpty() ? null : userModelToUserResponseDTO(userModelOptional.get());
    }

    @Override
    public Boolean updateUser(UserRegisterRequestDTO userRegisterRequestDTO, String id) {
        Optional<UserModel> userModel = userRepository.findById(id);
        if (userModel.isEmpty()) {
            return null;
        }

        if (!Objects.equals(userRegisterRequestDTO.email(), userModel.get().getEmail())) {
            return false;
        }

        BeanUtils.copyProperties(userRegisterRequestDTO, userModel.get());
        userModel.get().setPassword(passwordEncoder.encode(userModel.get().getPassword()));
        userRepository.save(userModel.get());
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteUser(String id) {
        Optional<UserModel> userModel = userRepository.findById(id);
        if (userModel.isEmpty()) {
            return false;
        }

        userRepository.delete(userModel.get());
        return true;
    }

    private UserResponseDTO userModelToUserResponseDTO(UserModel userModel) {
        return new UserResponseDTO(userModel);
    }

    private void saveUserToken(UserModel userModel, String jwtToken) {
        Token token = new Token(userModel, jwtToken, TokenType.BEARER, false, false);
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(UserModel userModel) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUserModel(userModel.getId());

        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }
}
