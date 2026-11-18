package com.daon.daonpay.domain.place.repository;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * 사용처 목록 검색. 조건이 null이면 해당 조건은 무시

     * @param region      시/도 또는 시/군/구 중 하나와 일치
     * @param keyword     사용처 이름 LIKE 패턴 (예: "%마트%")
     * @param productName 해당 상품을 보유한 사용처만 조회하는 LIKE 패턴
     */

    @Query("""
        select p from Place p
        where (:category is null or p.category = :category)
          and (:region is null or trim(p.sido) = :region or trim(p.sigungu) = :region)
          and (:keyword is null or p.name like :keyword escape '!')
          and (:status is null or p.status = :status)
          and (:productName is null or exists (
                select 1 from PlaceProduct pp
                where pp.place = p and pp.name like :productName escape '!'))
        """)
    Page<Place> search(@Param("category") PlaceCategory category,
                       @Param("region") String region,
                       @Param("keyword") String keyword,
                       @Param("productName") String productName,
                       @Param("status") PlaceStatus status,
                       Pageable pageable);
}