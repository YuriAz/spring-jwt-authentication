package com.ycash.server.repository;

import com.ycash.server.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    @Query("""
            select t from tokens t inner join users u\s
            on t.userModel.id = u.id\s
            where u.id = :userId and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUserModel(String userId);

    Optional<Token> findByToken(String token);
}
