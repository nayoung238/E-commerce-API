package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_coupon", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "coupon_name")
    private Map<Long, String> coupons;

    @Builder(access = AccessLevel.PRIVATE)
    private Account(String loginId, String password, String name, Map<Long, String> coupons) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.coupons = coupons;
    }

    public static Account of(SignUpRequestDto signUpRequest) {
        return Account.builder()
                .loginId(signUpRequest.loginId())
                .password(signUpRequest.password())
                .name(signUpRequest.name())
                .build();
    }

    public void addCoupon(Long couponId, String couponName) {
        this.coupons.put(couponId, couponName);
    }
}
