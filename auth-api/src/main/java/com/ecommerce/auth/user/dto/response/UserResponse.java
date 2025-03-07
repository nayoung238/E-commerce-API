package com.ecommerce.auth.user.dto.response;

import com.ecommerce.auth.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(

    long userId,
    String loginId,
    String name
) {

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .build();
    }
}