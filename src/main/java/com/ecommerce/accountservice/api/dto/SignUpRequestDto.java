package com.ecommerce.accountservice.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpRequestDto {

    private final String email;
    private final String password;
    private final String name;

    @Builder(access =  AccessLevel.PRIVATE)
    private SignUpRequestDto(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static SignUpRequestDto of(String email, String password, String name) {
        return SignUpRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }
}
