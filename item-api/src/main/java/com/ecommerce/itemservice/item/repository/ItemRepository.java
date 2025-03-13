package com.ecommerce.itemservice.item.repository;

import com.ecommerce.itemservice.item.entity.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id=:itemId")
    Optional<Item> findByIdWithPessimisticLock(@Param("itemId") Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select i from Item i where i.id=:itemId")
    Optional<Item> findByIdWithOptimisticLock(@Param("itemId") Long id);

    List<Item> findAllByName(String name);

    @Query("SELECT i FROM Item i WHERE i.name LIKE CONCAT('%', :name, '%')")
    List<Item> findAllByNameContaining(@Param("name") String name);

    List<Item> findAllByNameIn(List<String> name);

    List<Item> findAllByPriceBetween(long lowestPrice, long highestPrice);

    @Query("SELECT i FROM Item i WHERE i.name LIKE CONCAT('%', :name, '%') AND i.price BETWEEN :lowestPrice AND :highestPrice")
    List<Item> findAllByNameContainingAndPriceBetween(@Param("name") String name,
                                                      @Param("lowestPrice") long lowestPrice,
                                                      @Param("highestPrice") long highestPrice);
}
