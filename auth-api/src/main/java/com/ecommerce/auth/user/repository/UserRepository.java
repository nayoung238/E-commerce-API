package com.ecommerce.auth.user.repository;

import com.ecommerce.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByLoginId(String loginId);

	Optional<User> findByLoginId(String loginId);
}
