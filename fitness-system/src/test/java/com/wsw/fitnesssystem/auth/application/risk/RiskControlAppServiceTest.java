package com.wsw.fitnesssystem.auth.application.risk;

import com.wsw.fitnesssystem.auth.risk.application.RiskControlService;
import com.wsw.fitnesssystem.shared.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author loriyuhv
 * @version 1.0 2026/8/11 13:04
 * @since 1.0
 */
@SpringBootTest
class RiskControlAppServiceTest {

    @Autowired
    private RiskControlService service;

    @Test
    void preCheck() {
        // 1. 用户名为空，抛出非法参数异常
        final String username = "";
        assertThrows(BizException.class, () -> service.preCheck(username));
    }

    @Test
    void onFail() {
    }

    @Test
    void onSuccess() {
    }
}