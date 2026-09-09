package com.wsw.fitnesssystem.data_exchange.infrastructure.exception;

import com.wsw.fitnesssystem.shared.exception.BizException;
import com.wsw.fitnesssystem.shared.response.ResultCode;

/**
 * 导入基础设施异常。
 * <p>
 * 封装基础设施层（文件解析、IO、缓存等）的技术异常。
 * <p>
 * <b>适用场景：</b>
 * <ul>
 *   <li>文件解析失败（格式损坏、密码保护、版本不兼容等）</li>
 *   <li>文件 IO 异常（文件不存在、读写失败等）</li>
 *   <li>基础设施资源异常（线程池满、连接超时等）</li>
 * </ul>
 * <p>
 * <b>使用原则：</b>
 * <ul>
 *   <li>由基础设施层抛出，应用层捕获并转换为任务状态（FAILED）</li>
 *   <li>应用层不应主动抛出此异常</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 22:38
 * @since 1.0
 */
public class ImportInfrastructureException extends BizException {

    public ImportInfrastructureException(ResultCode resultCode) {
        super(resultCode);
    }

    public ImportInfrastructureException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ImportInfrastructureException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    public ImportInfrastructureException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

}
