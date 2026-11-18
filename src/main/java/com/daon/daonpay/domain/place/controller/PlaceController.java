package com.daon.daonpay.domain.place.controller;

import com.daon.daonpay.domain.place.dto.request.PlaceCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceProductCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceStatusUpdateRequest;
import com.daon.daonpay.domain.place.dto.response.PlacePageResponse;
import com.daon.daonpay.domain.place.dto.response.PlaceResponse;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.service.PlaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    // 명세서 기준 페이지당 최대 데이터 개수
    private static final int MAX_SIZE = 100;

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    // 사용처 목록 조회. page는 0부터 시작, size는 1~100 (기본 20)
    @GetMapping
    public PlacePageResponse search(@RequestParam(required = false) PlaceCategory category,
                                    @RequestParam(required = false) String region,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String productName,
                                    @RequestParam(required = false) PlaceStatus status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_SIZE),
                Sort.by(Sort.Direction.DESC, "id"));
        return placeService.search(category, region, keyword, productName, status, pageable);
    }

    // 사용처 상세 조회
    @GetMapping("/{placeId}")
    public PlaceResponse get(@PathVariable Long placeId) {
        return placeService.get(placeId);
    }

    // 사용처 등록 (관리자)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse create(@Valid @RequestBody PlaceCreateRequest req) {
        return placeService.create(req);
    }

    // 사용처 운영 상태 변경 (관리자)
    @PatchMapping("/{placeId}/status")
    public PlaceResponse updateStatus(@PathVariable Long placeId,
                                      @Valid @RequestBody PlaceStatusUpdateRequest req) {
        return placeService.updateStatus(placeId, req);
    }

    // 사용처에 상품 추가 (관리자)
    @PostMapping("/{placeId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceResponse addProduct(@PathVariable Long placeId,
                                    @Valid @RequestBody PlaceProductCreateRequest req) {
        return placeService.addProduct(placeId, req);
    }

    // 사용처의 상품 삭제 (관리자)
    @DeleteMapping("/{placeId}/products/{productId}")
    public PlaceResponse removeProduct(@PathVariable Long placeId,
                                       @PathVariable Long productId) {
        return placeService.removeProduct(placeId, productId);
    }
}