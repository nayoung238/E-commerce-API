package com.ecommerce.auth.user.entity;

import com.ecommerce.auth.auth.enums.BaseRole;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import com.ecommerce.auth.user.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BaseRole role;

    public static User of(SignUpRequestDto signUpRequest) {
        return User.builder()
                .loginId(signUpRequest.loginId())
                .password(signUpRequest.password())
                .name(signUpRequest.name())
                .role(BaseRole.USER)
                .build();
    }

    public void verifyPasswordMatching(String requestedPassword) {
        if (!password.equals(requestedPassword)) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }
}
