package com.wsw.fitnesssystem.handle_excel.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 用户导入结果对象
 * <p>由 user 模块返回，handle_excel 模块根据结果收集错误信息</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 12:47
 * @since 1.0
 */
@Getter
@Builder
@ToString
public class UserImportResult {

    /** Excel 行号（用于错误定位） */
    private Integer rowIndex;

    /** 用户名（便于排查） */
    private String username;

    /** 是否成功 */
    private boolean success;

    /** 错误原因（仅当 success=false 时有效） */
    private String errorMessage;

    /** 生成的用户ID（仅当 success=true 时有效） */
    private Long userId;

}
