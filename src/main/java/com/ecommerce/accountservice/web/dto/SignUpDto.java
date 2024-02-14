package com.ecommerce.accountservice.web.dto;

import lombok.Data;

@Data
public class SignUpDto {

    private String email;
    private String password;
    private String name;
}
