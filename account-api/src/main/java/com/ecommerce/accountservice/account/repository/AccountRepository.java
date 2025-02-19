package com.ecommerce.accountservice.account.repository;

import com.ecommerce.accountservice.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

	boolean existsByLoginId(String loginId);
}
