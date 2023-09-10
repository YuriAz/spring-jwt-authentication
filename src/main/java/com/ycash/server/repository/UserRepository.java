package com.ycash.server.repository;

import com.ycash.server.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, String> {

    Optional<UserModel> findUserByEmail(String email);

    Optional<UserModel> findByEmail(String email);
}
