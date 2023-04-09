package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.discount.DiscountCode;
import com.nayoung.itemservice.domain.shop.Shop;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
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

    private Item (ItemCreationRequest itemCreationRequest, Shop shop) {
        this.shop = shop;
        this.name = itemCreationRequest.getName();
        this.price = itemCreationRequest.getPrice();
        this.stock = itemCreationRequest.getStock();
        this.discountPercentage = DiscountCode.NONE.percentage;
    }

    public static Item fromItemCreationRequestAndShopEntity(ItemCreationRequest itemCreationRequest, Shop shop) {
        return new Item(itemCreationRequest, shop);
    }

    public void decreaseStock(Long quantity) {
        if(this.stock >= quantity)
            this.stock -= quantity;
        else
            throw new StockException(ExceptionCode.INSUFFICIENT_STOCK_EXCEPTION);
    }

    public void increaseStock(Long quantity) {
        this.stock += quantity;
    }

    public void update(ItemInfoUpdateRequest request) {
        this.name = request.getName();
        this.price = request.getPrice();
        this.stock += request.getAdditionalQuantity();
        this.discountPercentage = request.getDiscountPercentage();
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = Math.min(discountPercentage, 100);
    }
}
