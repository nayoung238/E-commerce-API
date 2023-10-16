package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.ItemDto;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;

@Entity @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private String name;
    private Long price;
    private Integer discountPercentage;
    private Long stock;

    public static Item fromItemDtoAndShopEntity(ItemDto itemDto, Shop shop) {
        return Item.builder()
                .shop(shop)
                .name(itemDto.getName())
                .price(itemDto.getPrice())
                .discountPercentage((itemDto.getDiscountPercentage() == null) ? 0 : itemDto.getDiscountPercentage())
                .stock(itemDto.getStock())
                .build();
    }

    public void updateStock(Long quantity) {
        if(quantity >= 0)  // production
            this.stock += quantity;
        else if(this.stock >= -quantity)  // consumption
            this.stock += quantity;
        else
            throw new StockException(ExceptionCode.INSUFFICIENT_STOCK_EXCEPTION);
    }

    public void update(ItemInfoUpdateRequest request) {
        if(StringUtils.hasText(request.getName())) {
            this.name = request.getName();
        }
        if(request.getPrice() != null) {
            this.price = request.getPrice();
        }
        if(request.getAdditionalQuantity() != null) {
            this.stock += request.getAdditionalQuantity();
        }
        if(request.getDiscountPercentage() != null) {
            this.discountPercentage = request.getDiscountPercentage();
        }
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = Math.min(discountPercentage, 100);
    }
}
