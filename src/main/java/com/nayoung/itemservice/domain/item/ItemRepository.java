package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.shop.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id=:id")
    Optional<Item> findByIdWithPessimisticLock(Long id);

    List<Item> findAllByName(String name);

    Optional<Item> findByShopAndName(Shop shop, String name);
}
