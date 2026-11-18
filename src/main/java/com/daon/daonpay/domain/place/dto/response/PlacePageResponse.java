package com.daon.daonpay.domain.place.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 사용처 목록 페이징 응답.
 * API 명세서의 PageMetadata 형식(content/totalElements/totalPages/page/size)을 따른다.
 */

public record PlacePageResponse(
        List<PlaceResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static PlacePageResponse from(Page<PlaceResponse> result) {
        return new PlacePageResponse(
                result.getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }
}