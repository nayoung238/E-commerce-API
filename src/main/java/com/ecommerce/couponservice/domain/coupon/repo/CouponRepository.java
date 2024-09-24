package com.ecommerce.couponservice.domain.coupon.repo;

import com.ecommerce.couponservice.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    boolean existsByName(String name);
}