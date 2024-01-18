package com.nayoung.itemservice.domain.item.repository;

import com.nayoung.itemservice.domain.item.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id=:id")
    Optional<Item> findByIdWithPessimisticLock(Long id);

    List<Item> findAllByName(String name);
}
