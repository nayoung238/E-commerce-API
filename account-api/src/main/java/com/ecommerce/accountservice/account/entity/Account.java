package com.ecommerce.accountservice.account.entity;

import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

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
    private Account(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    public static Account of(SignUpRequestDto signUpRequest) {
        return Account.builder()
                .loginId(signUpRequest.loginId())
                .password(signUpRequest.password())
                .name(signUpRequest.name())
                .build();
    }
}
