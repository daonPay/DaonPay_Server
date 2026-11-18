package com.daon.daonpay.domain.place.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 상품 추가 요청
public record PlaceProductCreateRequest(
        @NotBlank @Size(max = 100) String productName   // PlaceProduct.name 컬럼 길이(100)와 동일
) {}