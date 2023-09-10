package com.ycash.server.model;

import com.ycash.server.dto.UserRegisterRequestDTO;
import com.ycash.server.enums.Role;
import com.ycash.server.util.YUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Data
@EqualsAndHashCode(of = "id")
public class UserModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(updatable = false, unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean mfaEnabled;

    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "userModel")
    private List<Token> tokens;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public UserModel() {
    }

    public UserModel(UserRegisterRequestDTO userRegisterRequestDTO) {
        this.firstName = userRegisterRequestDTO.firstName();
        this.lastName = userRegisterRequestDTO.lastName();
        this.email = userRegisterRequestDTO.email();
        this.password = userRegisterRequestDTO.password();
        this.role = userRegisterRequestDTO.role();
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @PrePersist
    public void prePersist() {
        this.firstName = YUtils.formatName(this.firstName);
        this.lastName = YUtils.formatName(this.lastName);
        this.email = this.email.toLowerCase();
    }

    @PreUpdate
    public void preUpdate() {
        this.firstName = YUtils.formatName(this.firstName);
        this.lastName = YUtils.formatName(this.lastName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
