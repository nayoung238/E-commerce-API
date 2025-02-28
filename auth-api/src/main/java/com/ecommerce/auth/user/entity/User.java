package com.ecommerce.auth.user.entity;

import com.ecommerce.auth.user.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    public static User of(SignUpRequestDto signUpRequest) {
        return User.builder()
                .loginId(signUpRequest.loginId())
                .password(signUpRequest.password())
                .name(signUpRequest.name())
                .build();
    }
}
