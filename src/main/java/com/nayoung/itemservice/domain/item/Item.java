package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.web.dto.ItemDto;
import com.nayoung.itemservice.web.dto.ItemInfoUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

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

    private String name;

    private Long stock;

    public static Item fromItemDto(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
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
        if(request.getAdditionalQuantity() != null) {
            this.stock += request.getAdditionalQuantity();
        }
    }
}
