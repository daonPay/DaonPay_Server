package com.daon.daonpay.domain.place.controller;

import com.daon.daonpay.domain.place.dto.response.WhitelistPageResponse;
import com.daon.daonpay.domain.place.dto.response.WhitelistStatusHistoryResponse;
import com.daon.daonpay.domain.place.service.PlaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whitelists")
@RequiredArgsConstructor
public class WhitelistController {

    private static final int MAX_SIZE = 100;

    private final PlaceService placeService;

    @GetMapping
    public WhitelistPageResponse findAvailable(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_SIZE), Sort.by(Sort.Direction.ASC, "name"));
        return placeService.findAvailableWhitelists(region, pageable);
    }

    @GetMapping("/{placeId}/status-history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<WhitelistStatusHistoryResponse> statusHistory(@PathVariable Long placeId) {
        return placeService.findStatusHistory(placeId);
    }
}
