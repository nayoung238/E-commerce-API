package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.CityCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findAllByCityCode(CityCode cityCode);
    Optional<Shop> findByName(String name);
    Optional<Shop> findByCityCodeAndName(CityCode cityCode, String name);
    Long countByName(String name);
}
