package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.LocationCode;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ShopException;
import com.nayoung.itemservice.web.dto.ShopDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopDto create(ShopDto shopDto) {
        try {
            Shop shop = Shop.fromShopDto(shopDto);
            shopRepository.save(shop);
        } catch (DataIntegrityViolationException e) {
            throw new ShopException(ExceptionCode.DUPLICATE_NAME);
        }
        Shop savedShop = shopRepository.findByLocationCodeAndName(LocationCode.getLocationCode(shopDto.getLocation()), shopDto.getName())
                .orElseThrow(() -> new ShopException(ExceptionCode.NOT_FOUND_SHOP));
        return ShopDto.fromShop(savedShop);
    }

    public Shop findShopById(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ShopException(ExceptionCode.NOT_FOUND_SHOP));
    }

    public List<Shop> findAllShopByLocation(String location) {
    return shopRepository.findAllByLocationCode(LocationCode.getLocationCode(location));
    }
}