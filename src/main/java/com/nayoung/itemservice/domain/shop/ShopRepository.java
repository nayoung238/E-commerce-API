package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.CityCode;
import com.nayoung.itemservice.domain.shop.location.ProvinceCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findAllByLocationProvinceAndLocationCity(ProvinceCode province, CityCode city);
}
