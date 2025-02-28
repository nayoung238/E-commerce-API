package com.ecommerce.auth.user.repository;

import com.ecommerce.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByLoginId(String loginId);
}
