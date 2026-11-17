package com.daon.daonpay.domain.place.service;

import com.daon.daonpay.domain.place.dto.request.PlaceCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceProductCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceStatusUpdateRequest;
import com.daon.daonpay.domain.place.dto.response.PlacePageResponse;
import com.daon.daonpay.domain.place.dto.response.PlaceResponse;
import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    // 사용처 목록 검색. 공백 조건은 null로 바꿔 검색 조건에서 제외한다.
    public PlacePageResponse search(PlaceCategory category, String region, String keyword,
                                    String productName, PlaceStatus status,
                                    Pageable pageable) {
        Page<PlaceResponse> result = placeRepository
                .search(category, blankToNull(region), toLikePattern(keyword),
                        toLikePattern(productName), status, pageable)
                .map(PlaceResponse::from);
        return PlacePageResponse.from(result);
    }

    // 사용처 상세 조회
    public PlaceResponse get(Long placeId) {
        return PlaceResponse.from(find(placeId));
    }

    // 사용처 등록
    @Transactional
    public PlaceResponse create(PlaceCreateRequest req) {
        Place place = new Place(req.placeName(), req.category(), req.sido(),
                req.sigungu(), req.address(), req.productNames());
        return PlaceResponse.from(placeRepository.save(place));
    }

    // 사용처 운영 상태 변경
    @Transactional
    public PlaceResponse updateStatus(Long placeId, PlaceStatusUpdateRequest req) {
        Place place = find(placeId);
        place.changeStatus(req.status());
        return PlaceResponse.from(place);
    }

    // 사용처에 상품 추가. 같은 이름의 상품이 이미 있으면 예외
    @Transactional
    public PlaceResponse addProduct(Long placeId, PlaceProductCreateRequest req) {
        Place place = find(placeId);
        String productName = req.productName().trim();

        if (place.hasProduct(productName)) {
            throw new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        place.addProduct(productName);
        placeRepository.flush(); // 새 상품의 id를 응답에 담기 위해 먼저 반영
        return PlaceResponse.from(place);
    }

    // 사용처의 상품 삭제. 해당 상품이 없으면 예외
    @Transactional
    public PlaceResponse removeProduct(Long placeId, Long productId) {
        Place place = find(placeId);
        if (!place.removeProduct(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return PlaceResponse.from(place);
    }

    /**
     * 이용 가능한 사용처를 조회한다.
     * 결제 등 다른 도메인에서 사용처 상태를 검증할 때 호출한다.
     */

    public Place getUsablePlace(Long placeId) {
        Place place = find(placeId);
        if (!place.isAvailable()) {
            throw new CustomException(ErrorCode.PLACE_UNAVAILABLE);
        }
        return place;
    }

    private Place find(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // 검색어를 LIKE 패턴("%검색어%")으로 변환한다. 비어 있으면 null
    private String toLikePattern(String value) {
        return blankToNull(value) == null ? null : "%" + value.trim() + "%";
    }
}