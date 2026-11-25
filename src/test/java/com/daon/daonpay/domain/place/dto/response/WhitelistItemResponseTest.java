package com.daon.daonpay.domain.place.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.daon.daonpay.domain.place.entity.Place;
import com.daon.daonpay.domain.place.entity.PlaceCategory;
import java.util.List;
import org.junit.jupiter.api.Test;

class WhitelistItemResponseTest {

    @Test
    void shelterIsExposedWithKoreanWhitelistLabel() {
        Place shelter = new Place("중앙 대피소", PlaceCategory.SHELTER,
                "대구광역시", "달서구", "달구벌대로 1", List.of());

        WhitelistItemResponse response = WhitelistItemResponse.from(shelter);

        assertThat(response.typeLabel()).isEqualTo("대피소");
        assertThat(response.region()).isEqualTo("대구광역시 달서구");
    }
}
