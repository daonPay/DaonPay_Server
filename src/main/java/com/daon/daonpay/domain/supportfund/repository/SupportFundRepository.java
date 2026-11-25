package com.daon.daonpay.domain.supportfund.repository;

import com.daon.daonpay.domain.supportfund.entity.SupportFund;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportFundRepository extends JpaRepository<SupportFund, Long> {

    Optional<SupportFund> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fund from SupportFund fund where fund.userId = :userId")
    Optional<SupportFund> findByUserIdForUpdate(@Param("userId") Long userId);
}
