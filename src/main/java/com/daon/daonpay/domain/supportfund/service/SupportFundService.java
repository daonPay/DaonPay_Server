package com.daon.daonpay.domain.supportfund.service;

import com.daon.daonpay.domain.supportfund.dto.response.SupportFundResponse;
import com.daon.daonpay.domain.supportfund.entity.SupportFund;
import com.daon.daonpay.domain.supportfund.repository.SupportFundRepository;
import com.daon.daonpay.domain.user.repository.UserRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupportFundService {

    private final SupportFundRepository supportFundRepository;
    private final UserRepository userRepository;

    @Transactional
    public SupportFundResponse grant(Long userId, long amount) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_SUPPORT_FUND_AMOUNT);
        }
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        SupportFund fund = supportFundRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> supportFundRepository.save(new SupportFund(userId)));
        try {
            fund.grant(amount);
        } catch (ArithmeticException e) {
            throw new CustomException(ErrorCode.INVALID_SUPPORT_FUND_AMOUNT);
        }
        return SupportFundResponse.from(fund);
    }

    @Transactional(readOnly = true)
    public SupportFundResponse getMine(Long userId) {
        SupportFund fund = supportFundRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUPPORT_FUND_NOT_FOUND));
        return SupportFundResponse.from(fund);
    }
}
