package com.ecommerce.couponservice.internalevent.couponissuanceresult;

import com.ecommerce.couponservice.internalevent.InternalEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CouponIssuanceResultInternalEvent {

    @EmbeddedId
    private CouponIssuanceResultId id;

    @Enumerated(EnumType.STRING)
    private InternalEventStatus publicationStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponIssuanceResultInternalEvent(CouponIssuanceResultId id, InternalEventStatus publicationStatus) {
        this.id = id;
        this.publicationStatus = publicationStatus;
    }

    public static CouponIssuanceResultInternalEvent init(long couponId, long accountId) {
        return CouponIssuanceResultInternalEvent.builder()
                .id(CouponIssuanceResultId.of(couponId, accountId))
                .publicationStatus(InternalEventStatus.init)
                .build();
    }

    public void updatePublicationStatus(InternalEventStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }
}
