package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.CityCode;
import com.nayoung.itemservice.domain.shop.location.Location;
import com.nayoung.itemservice.domain.shop.location.ProvinceCode;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ShopException;
import com.nayoung.itemservice.web.dto.ShopCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public void create(ShopCreationRequest request) {
        Location location = new Location(request.getProvince(), request.getCity());
        if(isExistShopName(request.getName())) {
            throw new ShopException(ExceptionCode.ALREADY_EXIST_SHOP_NAME);
        }
        Shop shop = Shop.fromLocationAndName(location, request.getName());
        shopRepository.save(shop);
    }

    private boolean isExistShopName(String name) {
        return shopRepository.findByName(name).isPresent();
    }

    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(ExceptionCode.NOT_FOUND_SHOP));
    }

    public List<Shop> findShops(String province, String city) {
        ProvinceCode provinceCode = ProvinceCode.getProvinceCode(province);
        CityCode cityCode = CityCode.getCityCode(city);

        if(provinceCode == ProvinceCode.NONE && cityCode == CityCode.NONE) return shopRepository.findAll();
        return shopRepository.findAllByLocationProvinceAndLocationCity(provinceCode, cityCode);
    }
}