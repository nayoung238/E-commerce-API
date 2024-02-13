package com.ecommerce.itemservice.domain.shop;

import com.ecommerce.itemservice.domain.shop.location.LocationCode;
import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.exception.ShopException;
import com.ecommerce.itemservice.web.dto.ShopDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopDto create(ShopDto shopDto) {
        Shop shop = null;
        try {
            shop = shopRepository.save(Shop.fromShopDto(shopDto));
        } catch (DataIntegrityViolationException e) {
            throw new ShopException(ExceptionCode.DUPLICATE_NAME);
        }
        return ShopDto.fromShop(shop);
    }

    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(ExceptionCode.NOT_FOUND_SHOP));
    }

    public List<Shop> findAllShopByLocation(String location) {
    return shopRepository.findAllByLocationCode(LocationCode.getLocationCode(location));
    }
}