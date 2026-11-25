package com.daon.daonpay.domain.place.repository;

import com.daon.daonpay.domain.place.entity.WhitelistStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhitelistStatusHistoryRepository extends JpaRepository<WhitelistStatusHistory, Long> {

    List<WhitelistStatusHistory> findByPlaceIdOrderByChangedAtDesc(Long placeId);
}
