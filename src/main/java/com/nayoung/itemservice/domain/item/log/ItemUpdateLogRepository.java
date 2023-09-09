package com.nayoung.itemservice.domain.item.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemUpdateLogRepository extends JpaRepository<ItemUpdateLog, Long> {

    List<ItemUpdateLog> findAllByOrderId(Long orderId);
    List<ItemUpdateLog> findAllByItemIdAndOrderStatus(Long itemId, ItemUpdateStatus itemUpdateStatus);
}