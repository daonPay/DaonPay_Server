package com.daon.daonpay.domain.place.dto.request;

import jakarta.validation.constraints.NotBlank;

// 상품 추가 요청

public record PlaceProductCreateRequest(@NotBlank String productName) {}