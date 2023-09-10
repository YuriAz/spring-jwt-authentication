package com.ycash.server.controller;

import com.ycash.server.dto.*;
import com.ycash.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT')")
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<Object> registerUser(@RequestBody @Valid UserRegisterRequestDTO userRegisterRequestDTO) {
        try {
            AuthenticationResponseDTO user = userService.registerUser(userRegisterRequestDTO);

            if (userRegisterRequestDTO.mfaEnabled()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(user);
            }

            return ResponseEntity.accepted().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("O e-mail informado já está em uso.");
        }
    }

    @PostMapping("/auth/authenticate")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<Object> authenticateUser(@RequestBody @Valid AuthenticationRequestDTO authenticationRequestDTO) {
        AuthenticationResponseDTO token = userService.authenticate(authenticationRequestDTO);

        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/auth/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        userService.refreshToken(request, response);
    }

    @PostMapping("/auth/verify")
    public ResponseEntity<Object> verifyCode(@RequestBody VerificationRequestDTO verificationRequestDTO) {
        return ResponseEntity.ok(userService.verifyCode(verificationRequestDTO));
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();

        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum usuário encontrado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        UserResponseDTO user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserRegisterRequestDTO userRegisterRequestDTO, @PathVariable String id) {
        Boolean user = userService.updateUser(userRegisterRequestDTO, id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        } else if (!user) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Não é possível alterar o e-mail.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Alterações realizadas com sucesso!");
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        Boolean isUserDeleted = userService.deleteUser(id);

        if (!isUserDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body("Usuário excluído com sucesso!");
    }
}
