package com.daon.daonpay.domain.place.entity;

import jakarta.persistence.*;

/**
 * 사용처가 보유한 상품
 * 수량은 관리하지 않으며, 같은 사용처 안에서 상품명은 중복될 수 없음
 */

@Entity
@Table(
        name = "place_products",
        uniqueConstraints = @UniqueConstraint(
                name = PlaceProduct.UNIQUE_PLACE_NAME, columnNames = {"place_id", "name"})
)
public class PlaceProduct {

    public static final String UNIQUE_PLACE_NAME = "uk_place_products_place_name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품을 보유한 사용처
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id")
    private Place place;

    // 상품명
    @Column(nullable = false, length = 100)
    private String name;

    protected PlaceProduct() {
        // JPA 기본 생성자
    }

    // Place.addProduct()를 통해서만 생성하도록 package-private으로 둔다.
    PlaceProduct(Place place, String name) {
        this.place = place;
        this.name = name;
    }

    public Long getId() { return id; }
    public Place getPlace() { return place; }
    public String getName() { return name; }
}