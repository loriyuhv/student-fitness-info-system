package com.wsw.fitnesssystem.data_exchange.domain.exception;

import com.wsw.fitnesssystem.shared.application.exception.BizException;
import com.wsw.fitnesssystem.shared.interfaces.web.response.ErrorCode;

/**
 * 导入任务被用户主动取消异常
 *
 * @author loriyuhv
 * @version 1.0 2026/8/31 14:10
 * @since 1.0
 */
public class ImportCancelledException extends BizException {

    public ImportCancelledException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
