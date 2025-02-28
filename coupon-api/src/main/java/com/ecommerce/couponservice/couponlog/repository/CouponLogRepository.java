package com.ecommerce.couponservice.couponlog.repository;

import com.ecommerce.couponservice.couponlog.entity.CouponLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CouponLogRepository extends CrudRepository<CouponLog, Long> {

	List<CouponLog> findAllByUserId(Long userId);
}
