package com.ecommerce.auth.auth.repository;

import com.ecommerce.auth.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

	boolean existsByRefreshToken(String refreshToken);
}
