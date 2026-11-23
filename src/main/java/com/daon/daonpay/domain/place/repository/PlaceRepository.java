package com.daon.daonpay.domain.place.repository;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from Place p where p.id = :id")
    Optional<Place> findByIdForShare(@Param("id") Long id);

    /**
     * 사용처 목록 검색. 조건이 null이면 해당 조건은 무시한다.

     * @param region      시/도 또는 시/군/구 중 하나와 일치 (앞뒤 공백 제거된 값)
     * @param keyword     사용처 이름 LIKE 패턴. %, _ 는 서비스에서 '!'로 이스케이프되어 전달된다.
     * @param productName 해당 상품을 보유한 사용처만 조회하는 LIKE 패턴 (이스케이프 규칙 동일)
     */

    @Query("""
        select p from Place p
        where (:category is null or p.category = :category)
          and (:region is null or p.sido = :region or p.sigungu = :region)
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