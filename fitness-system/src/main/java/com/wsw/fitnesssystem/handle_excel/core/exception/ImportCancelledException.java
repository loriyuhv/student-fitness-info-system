package com.wsw.fitnesssystem.handle_excel.core.exception;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 导入任务被用户主动取消异常
 *
 * @author loriyuhv
 * @version 1.0 2026/8/31 14:10
 * @since 1.0
 */
public class ImportCancelledException extends BizException {

    public ImportCancelledException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

}
