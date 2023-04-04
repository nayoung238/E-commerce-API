package com.nayoung.itemservice.domain.discount;

import com.nayoung.itemservice.web.dto.DiscountCreationRequest;

public interface DiscountService {

    void applyDiscount(DiscountCreationRequest request);
}
