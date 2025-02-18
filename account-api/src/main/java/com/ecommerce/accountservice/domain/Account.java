package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_coupon", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "coupon_name")
    private Map<Long, String> coupons;

    @Builder(access = AccessLevel.PRIVATE)
    private Account(String email, String password, String name, Map<Long, String> coupons) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.coupons = coupons;
    }

    public static Account of(SignUpRequestDto signUpRequest) {
        return Account.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .name(signUpRequest.getName())
                .coupons(new HashMap<>())
                .build();
    }

    public void addCoupon(Long couponId, String couponName) {
        this.coupons.put(couponId, couponName);
    }
}
