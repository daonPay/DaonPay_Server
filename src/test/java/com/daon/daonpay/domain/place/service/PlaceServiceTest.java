package com.daon.daonpay.domain.place.service;

import com.daon.daonpay.domain.place.dto.request.PlaceCreateRequest;
import com.daon.daonpay.domain.place.dto.request.PlaceProductCreateRequest;
import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import com.daon.daonpay.domain.place.entity.PlaceProduct;
import com.daon.daonpay.domain.place.repository.PlaceRepository;
import com.daon.daonpay.global.exception.CustomException;
import com.daon.daonpay.global.exception.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceServiceTest {
    private final PlaceRepository repository = mock(PlaceRepository.class);
    private final PlaceService service = new PlaceService(repository);

    @Test
    void trimsRegionOnCreateAndEscapesSearchPatterns() {
        when(repository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var created = service.create(new PlaceCreateRequest(
                "마트", PlaceCategory.MERCHANT, " 경상북도 ", " 경산시 ", null, null));

        assertEquals("경상북도", created.sido());
        assertEquals("경산시", created.sigungu());

        var pageable = PageRequest.of(0, 20);
        when(repository.search(null, created.sigungu(), "%50!%%", "%A!_B!!%", null, pageable))
                .thenReturn(Page.empty(pageable));
        assertEquals(0, service.search(null, " 경산시 ", "50%", "A_B!", null, pageable).totalElements());
    }

    @Test
    void translatesDuplicateProductDetectedByDatabase() {
        stubPlace();
        doThrow(integrityViolation("place_products." + PlaceProduct.UNIQUE_PLACE_NAME))
                .when(repository).flush();

        var exception = assertThrows(CustomException.class,
                () -> service.addProduct(1L, new PlaceProductCreateRequest("생수")));
        assertEquals(ErrorCode.PRODUCT_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void preservesOtherIntegrityViolations() {
        stubPlace();
        var failure = integrityViolation("other_constraint");
        doThrow(failure).when(repository).flush();

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> service.addProduct(1L, new PlaceProductCreateRequest("생수"))));
    }

    private void stubPlace() {
        when(repository.findById(1L)).thenReturn(Optional.of(
                new Place("마트", PlaceCategory.MERCHANT, "경상북도", "경산시", null, null)));
    }

    private DataIntegrityViolationException integrityViolation(String constraintName) {
        return new DataIntegrityViolationException("constraint violation",
                new ConstraintViolationException("constraint violation", new SQLException(), constraintName));
    }
}
