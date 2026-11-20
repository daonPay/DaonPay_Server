package com.daon.daonpay.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // 발급·검증·저장이 모두 같은 UTC 기준 시각을 쓰도록 하는 공통 시계
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}