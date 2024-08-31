package com.ecommerce.itemservice.domain.item;

import com.ecommerce.itemservice.domain.item.dto.ItemRegisterRequest;
import com.ecommerce.itemservice.exception.ExceptionCode;
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

    @Builder
    private Item(Long id, String name, Long stock, Long price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static Item of(ItemRegisterRequest request) {
        return Item.builder()
                .name(request.getName())
                .stock(request.getStock())
                .price(request.getPrice())
                .build();
    }

    public void updateStock(Long quantity) {
        if(quantity >= 0 || this.stock >= -quantity) {   // production || consumption
            this.stock += quantity;
        }
        else {
            throw new IllegalArgumentException(ExceptionCode.INSUFFICIENT_STOCK_EXCEPTION.getMessage());
        }
    }
}
