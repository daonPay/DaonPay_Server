package com.daon.daonpay.domain.place.dto.response;

import com.daon.daonpay.domain.place.entity.PlaceProduct;

// 보유 상품 응답
public record PlaceProductResponse(Long productId, String productName) {

    public static PlaceProductResponse from(PlaceProduct product) {
        return new PlaceProductResponse(product.getId(), product.getName());
    }
}