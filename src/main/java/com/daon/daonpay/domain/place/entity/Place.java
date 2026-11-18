package com.daon.daonpay.domain.place.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * 대피소/사용처 엔티티 (기존 화이트리스트)
 * 재고 수량은 관리하지 않고, 보유 상품 목록(PlaceProduct)만 가짐
 */

@Entity
@Table(
        name = "places",
        indexes = {
                @Index(name = "idx_places_category", columnList = "category"),
                @Index(name = "idx_places_region", columnList = "sido, sigungu")
        }
)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용처 이름
    @Column(nullable = false, length = 100)
    private String name;

    // 카테고리 (대피소, 가맹점, 약국 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceCategory category;

    // 시/도 (예: 경상북도)
    @Column(nullable = false, length = 20)
    private String sido;

    // 시/군/구 (예: 경산시)
    @Column(nullable = false, length = 30)
    private String sigungu;

    // 상세 주소
    @Column(length = 200)
    private String address;

    // 운영 상태 (사용 가능 여부)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceStatus status;

    /**
     * 보유 상품 목록
     * - cascade/orphanRemoval: 목록에서 제거하면 DB에서도 삭제
     * - BatchSize: 목록 조회 시 상품을 한꺼번에 가져와 N+1을 방지
     */

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<PlaceProduct> products = new ArrayList<>();

    protected Place() {
        // JPA 기본 생성자
    }

    /**
     * 사용처를 생성한다. 최초 상태는 OPEN

     * @param productNames 초기 보유 상품명 목록 (null 가능, 공백/중복은 무시)
     */

    public Place(String name, PlaceCategory category, String sido, String sigungu,
                 String address, List<String> productNames) {
        this.name = name;
        this.category = category;
        this.sido = sido;
        this.sigungu = sigungu;
        this.address = address;
        this.status = PlaceStatus.OPEN;

        if (productNames != null) {
            productNames.stream()
                    .map(String::trim)
                    .filter(productName -> !productName.isEmpty())
                    .distinct()
                    .forEach(this::addProduct);
        }
    }

    // 현재 이용 가능한 사용처인지 여부
    public boolean isAvailable() {
        return status == PlaceStatus.OPEN;
    }

    // 운영 상태를 변경한다.
    public void changeStatus(PlaceStatus status) {
        this.status = status;
    }

    // 상품을 추가한다. (중복 검사는 서비스에서 hasProduct로 먼저 수행)
    public void addProduct(String productName) {
        products.add(new PlaceProduct(this, productName));
    }

    // 같은 이름의 상품을 이미 보유 중인지 확인한다.
    public boolean hasProduct(String productName) {
        return products.stream().anyMatch(product -> product.getName().equals(productName));
    }

    /**
     * 상품을 제거한다.

     * @return 제거되었으면 true, 해당 상품이 없으면 false
     */

    public boolean removeProduct(Long productId) {
        return products.removeIf(product -> productId.equals(product.getId()));
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public PlaceCategory getCategory() { return category; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public String getAddress() { return address; }
    public PlaceStatus getStatus() { return status; }
    public List<PlaceProduct> getProducts() { return products; }
}