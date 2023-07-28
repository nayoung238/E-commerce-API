package com.nayoung.itemservice.domain.discount;

import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.web.dto.DiscountCreationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final ItemRepository itemRepository;

    @Transactional
    public void applyDiscount(DiscountCreationRequest request) {
        Item item = itemRepository.findByIdWithPessimisticLock(request.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
        item.setDiscountPercentage(request.getDiscountPercentage());
    }
}
