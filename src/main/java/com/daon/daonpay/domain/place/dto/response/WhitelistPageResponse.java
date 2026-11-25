package com.daon.daonpay.domain.place.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record WhitelistPageResponse(
        List<WhitelistItemResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static WhitelistPageResponse from(Page<WhitelistItemResponse> result) {
        return new WhitelistPageResponse(result.getContent(), result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }
}
