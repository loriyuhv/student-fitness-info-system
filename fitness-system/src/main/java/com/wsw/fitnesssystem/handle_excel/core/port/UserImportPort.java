package com.wsw.fitnesssystem.handle_excel.core.port;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;

import java.util.List;

/**
 * 用户导入端口（由 handle_excel 模块定义，user 模块实现）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>定义导入模块对用户数据写入的依赖契约</li>
 *   <li>只暴露导入所需的最小数据集</li>
 *   <li>不依赖任何具体实现（本地 JVM 调用 / 远程 RPC 均透明）</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 12:02
 * @since 1.0
 */
public interface UserImportPort {

    /**
     * 批量注册用户（含扩展表）
     *
     * @param userDataList 用户导入数据列表
     * @return 每条数据的处理结果（成功/失败 + 错误原因 + 行号）
     */
    List<UserImportResult> batchRegister(List<UserImportData> userDataList);

}
