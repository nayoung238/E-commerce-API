package com.ecommerce.accountservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpDto {

    private String email;
    private String password;
    private String name;
}
