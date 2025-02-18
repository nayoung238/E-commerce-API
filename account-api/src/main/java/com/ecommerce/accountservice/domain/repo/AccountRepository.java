package com.ecommerce.accountservice.domain.repo;

import com.ecommerce.accountservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
