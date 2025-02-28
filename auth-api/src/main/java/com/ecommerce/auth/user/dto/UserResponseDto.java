package com.ecommerce.auth.user.dto;

import com.ecommerce.auth.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(

    long userId,
    String loginId,
    String name
) {

    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .build();
    }
}