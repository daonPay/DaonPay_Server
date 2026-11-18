package com.daon.daonpay.domain.place.controller;

import com.daon.daonpay.domain.place.service.PlaceService;
import com.daon.daonpay.global.config.SecurityConfig;
import com.daon.daonpay.global.exception.CustomAccessDeniedHandler;
import com.daon.daonpay.global.exception.CustomAuthenticationEntryPoint;
import com.daon.daonpay.global.jwt.JwtProvider;
import com.daon.daonpay.global.util.ErrorResponseWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceController.class)
@Import({SecurityConfig.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, ErrorResponseWriter.class})
class PlaceAuthorizationTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @ParameterizedTest
    @MethodSource("writeRequests")
    void regularUserCannotModifyPlaces(MockHttpServletRequestBuilder request) throws Exception {
        mvc.perform(request.with(user("member").roles("USER"))).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("writeRequests")
    void administratorCanModifyPlaces(MockHttpServletRequestBuilder request) throws Exception {
        mvc.perform(request.with(user("admin").roles("ADMIN"))).andExpect(status().is2xxSuccessful());
    }

    private static Stream<MockHttpServletRequestBuilder> writeRequests() {
        return Stream.of(
                post("/api/places").contentType("application/json")
                        .content("""
                                {"placeName":"마트","category":"MERCHANT","sido":"경상북도","sigungu":"경산시"}
                                """),
                patch("/api/places/1/status").contentType("application/json").content("{\"status\":\"OPEN\"}"),
                post("/api/places/1/products").contentType("application/json").content("{\"productName\":\"생수\"}"),
                delete("/api/places/1/products/2"));
    }
}
