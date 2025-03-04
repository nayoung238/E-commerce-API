package com.ecommerce.couponservice.couponlog.entity;

import com.ecommerce.couponservice.coupon.entity.Coupon;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CouponLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "coupon_id", nullable = false)
	private Coupon coupon;

	@Column(nullable = false)
	private Long userId;
}
