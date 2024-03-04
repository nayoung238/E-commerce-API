package com.ecommerce.itemservice.domain.item;

import com.ecommerce.itemservice.domain.item.dto.ItemRegisterRequest;
import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.exception.StockException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    public static Item of(ItemRegisterRequest request) {
        return Item.builder()
                .name(request.getName())
                .stock(request.getStock())
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
}
