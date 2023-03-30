package com.nayoung.itemservice.domain;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.ItemCreationRequest;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long price;
    private Long stock;

    private Item (ItemCreationRequest itemCreationRequest) {
        this.name = itemCreationRequest.getName();
        this.price = itemCreationRequest.getPrice();
        this.stock = itemCreationRequest.getStock();
    }

    public static Item fromItemCreationRequest(ItemCreationRequest itemCreationRequest) {
        return new Item(itemCreationRequest);
    }

    public void updateStock(Long quantity) {
        if(this.stock >= quantity)
            this.stock -= quantity;
        else
            throw new StockException(ExceptionCode.INSUFFICIENT_STOCK_EXCEPTION);
    }

    public void update(ItemInfoUpdateRequest request) {
        this.name = request.getName();
        this.price = request.getPrice();
        this.stock += request.getAdditionalQuantity();
    }
}
