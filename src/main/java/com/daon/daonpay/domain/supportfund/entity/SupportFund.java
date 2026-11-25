package com.daon.daonpay.domain.supportfund.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자별 재난지원금 지갑. 금액은 원 단위 정수로 저장한다. */
@Entity
@Table(name = "support_funds", uniqueConstraints = @UniqueConstraint(
        name = "uk_support_funds_user", columnNames = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private long totalGranted;

    @Column(nullable = false)
    private long balance;

    public SupportFund(Long userId) {
        this.userId = userId;
    }

    public void grant(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        totalGranted = Math.addExact(totalGranted, amount);
        balance = Math.addExact(balance, amount);
    }
}
