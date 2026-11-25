package com.daon.daonpay.domain.place.service;

import com.daon.daonpay.domain.place.dto.request.PlaceCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceProductCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceStatusUpdateRequest;
import com.daon.daonpay.domain.place.dto.response.PlacePageResponse;
import com.daon.daonpay.domain.place.dto.response.PlaceResponse;
import com.daon.daonpay.domain.place.dto.response.WhitelistItemResponse;
import com.daon.daonpay.domain.place.dto.response.WhitelistPageResponse;
import com.daon.daonpay.domain.place.dto.response.WhitelistStatusHistoryResponse;
import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import com.daon.daonpay.domain.place.entity.WhitelistStatusHistory;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.domain.place.repository.WhitelistStatusHistoryRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    // LIKE 검색용 이스케이프 문자. PlaceRepository 쿼리의 escape '!' 와 반드시 일치해야 함
    private static final String LIKE_ESCAPE = "!";

    // 같은 사용처 내 상품명 중복을 막는 유니크 제약 이름 (PlaceProduct 참고)
    private static final String PRODUCT_UNIQUE_CONSTRAINT = "uk_place_products_place_name";

    private final PlaceRepository placeRepository;
    private final WhitelistStatusHistoryRepository whitelistStatusHistoryRepository;

    public PlaceService(PlaceRepository placeRepository,
                        WhitelistStatusHistoryRepository whitelistStatusHistoryRepository) {
        this.placeRepository = placeRepository;
        this.whitelistStatusHistoryRepository = whitelistStatusHistoryRepository;
    }

    // 사용처 목록 검색. 공백 조건은 null로 바꿔 검색 조건에서 제외
    public PlacePageResponse search(PlaceCategory category, String region, String keyword,
                                    String productName, PlaceStatus status,
                                    Pageable pageable) {
        Page<PlaceResponse> result = placeRepository
                .search(category, normalize(region), toLikePattern(keyword),
                        toLikePattern(productName), status, pageable)
                .map(PlaceResponse::from);
        return PlacePageResponse.from(result);
    }

    // 사용처 상세 조회
    public PlaceResponse get(Long placeId) {
        return PlaceResponse.from(find(placeId));
    }

    /**
     * 사용처 등록.
     * 지역명은 저장 전에 trim해서, 앞뒤 공백 때문에 정확 일치 검색에서 빠지지 않도록 할 것
     * (region 검색어도 search()에서 같은 규칙으로 trim한다)
     */

    @Transactional
    public PlaceResponse create(PlaceCreateRequest req) {
        Place place = new Place(req.placeName(), req.category(),
                req.sido().trim(), req.sigungu().trim(),
                req.address(), req.productNames());
        return PlaceResponse.from(placeRepository.save(place));
    }

    // 사용처 운영 상태 변경
    @Transactional
    public PlaceResponse updateStatus(Long placeId, PlaceStatusUpdateRequest req) {
        Place place = find(placeId);
        PlaceStatus previousStatus = place.getStatus();
        place.changeStatus(req.status());
        if (previousStatus != req.status()) {
            whitelistStatusHistoryRepository.save(new WhitelistStatusHistory(
                    place.getId(), previousStatus, req.status(), Instant.now()));
        }
        return PlaceResponse.from(place);
    }

    /** 사용자 지역에서 현재 사용할 수 있는 대피소·사용처 화이트리스트를 조회한다. */
    public WhitelistPageResponse findAvailableWhitelists(String region, Pageable pageable) {
        return WhitelistPageResponse.from(placeRepository
                .search(null, normalize(region), null, null, PlaceStatus.OPEN, pageable)
                .map(WhitelistItemResponse::from));
    }

    public List<WhitelistStatusHistoryResponse> findStatusHistory(Long placeId) {
        find(placeId);
        return whitelistStatusHistoryRepository.findByPlaceIdOrderByChangedAtDesc(placeId).stream()
                .map(WhitelistStatusHistoryResponse::from)
                .toList();
    }

    /**
     * 사용처에 상품 추가.
     * - 순차 중복 요청: hasProduct 검사에서 걸러낼 것
     * - 동시 중복 요청: 둘 다 hasProduct를 통과할 수 있으므로,
     * flush에서 발생하는 유니크 제약 위반을 PRODUCT_ALREADY_EXISTS(409)로 변환
     */

    @Transactional
    public PlaceResponse addProduct(Long placeId, PlaceProductCreateRequest req) {
        Place place = find(placeId);
        String productName = req.productName().trim();

        if (place.hasProduct(productName)) {
            throw new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        place.addProduct(productName);

        try {
            placeRepository.flush(); // 새 상품의 id를 응답에 담기 위해 먼저 반영
        } catch (DataIntegrityViolationException e) {
            if (isDuplicateProduct(e)) {
                throw new CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS);
            }
            throw e; // 다른 무결성 오류는 그대로 전파
        }
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
     * 이용 가능한 사용처를 조회
     * 결제 등 다른 도메인에서 사용처 상태를 검증할 때 호출한
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

    // 앞뒤 공백을 제거하고, 비어 있으면 null을 반환한다.
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 검색어를 LIKE 패턴("%검색어%")으로 변환한다. 비어 있으면 null.
     * 검색어에 포함된 %, _ 와 이스케이프 문자 자체는 먼저 이스케이프해서,
     * 와일드카드가 아니라 일반 문자로 검색되도록 한다. (이스케이프 문자를 가장 먼저 처리해야 함)
     */

    private String toLikePattern(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String escaped = normalized
                .replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
        return "%" + escaped + "%";
    }

    // 예외 원인 중에 상품명 유니크 제약 위반이 있는지 확인
    private boolean isDuplicateProduct(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                String constraintName = cve.getConstraintName();
                if (constraintName != null
                        && constraintName.toLowerCase().contains(PRODUCT_UNIQUE_CONSTRAINT)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
