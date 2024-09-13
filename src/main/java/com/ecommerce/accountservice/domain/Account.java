package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private Account(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static Account of(SignUpRequestDto signUpRequest) {
        return Account.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .name(signUpRequest.getName())
                .build();
    }
}
