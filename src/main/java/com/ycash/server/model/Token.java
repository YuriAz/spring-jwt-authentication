package com.ycash.server.model;

import com.ycash.server.enums.TokenType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity(name = "tokens")
@Data
@EqualsAndHashCode(of = "id")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private boolean expired;
    private boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel userModel;

    public Token() {
    }

    public Token(UserModel userModel, String token, TokenType tokenType, boolean expired, boolean revoked) {
        this.userModel = userModel;
        this.token = token;
        this.tokenType = tokenType;
        this.expired = expired;
        this.revoked = revoked;
    }
}
