package com.wsw.fitnesssystem.handle_excel.application.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:45
 * @since 1.0
 */
@Data
public class UserExcelDTO {
    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("昵称")
    private String nickName;
}
