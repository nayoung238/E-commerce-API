package com.nayoung.itemservice.domain.item.repository;

import com.nayoung.itemservice.domain.item.ItemUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemUpdateLogRepository extends JpaRepository<ItemUpdateLog, Long> {

    List<ItemUpdateLog> findAllByEventId(String eventId);
}