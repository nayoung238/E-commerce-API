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
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    @Override
    public void create(ShopCreationRequest request) {
        Location location = new Location(request.getProvince(), request.getCity());
        Shop shop = Shop.fromLocation(location);
        shopRepository.save(shop);
    }

    @Override
    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(ExceptionCode.NOT_FOUND_SHOP));
    }

    @Override
    public List<Shop> findShops(String province, String city) {
        ProvinceCode provinceCode = ProvinceCode.getProvinceCode(province);
        CityCode cityCode = CityCode.getCityCode(city);

        if(provinceCode == ProvinceCode.NONE && cityCode == CityCode.NONE) return shopRepository.findAll();
        return shopRepository.findAllByLocationProvinceAndLocationCity(provinceCode, cityCode);
    }
}