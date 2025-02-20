package com.ecommerce.itemservice.item.entity;

import com.ecommerce.itemservice.item.dto.ItemRegisterRequest;
import com.ecommerce.itemservice.common.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @OptimisticLocking(type= OptimisticLockType.DIRTY) + @DynamicUpdate 사용 시 OOM 에러 발생할 수 있음
    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long stock;

    @Column(nullable = false)
    private Long price;

    @Builder(access = AccessLevel.PRIVATE)
    private Item(Long id, String name, Long stock, Long price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static Item of(ItemRegisterRequest request) {
        return Item.builder()
                .name(request.name())
                .stock(request.stock())
                .price(request.price())
                .build();
    }

    // Test 코드에서 사용
    public static Item of(String name, long stock, long price) {
        return Item.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .build();
    }

    public void increaseStock(Long quantity) {
        stock += quantity;
    }

    public void decreaseStock(Long quantity) {
        if(stock < quantity)
            throw new IllegalArgumentException(ExceptionCode.INSUFFICIENT_STOCK_EXCEPTION.getMessage());

        stock -= quantity;
    }
}
