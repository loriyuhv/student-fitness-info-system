package com.wsw.fitnesssystem.auth.domain.service.impl;

import com.wsw.fitnesssystem.auth.domain.service.LoginFailLimitDomainService;
import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/21 12:09
 * @since 1.0
 */
@Service
public class LoginFailLimitDomainServiceImpl implements LoginFailLimitDomainService {
    @Override
    public void checkFailCount(
        Long campusId,
        String username,
        int failCount,
        int maxFailCount
    ) {
        if (failCount >= maxFailCount) {
            throw new BizException(ResultCode.USER_LOGIN_ERROR);
        }
    }
}
