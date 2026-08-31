package com.wsw.fitnesssystem.handle_excel.application.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.wsw.fitnesssystem.handle_excel.core.model.RowIndexAware;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:45
 * @since 1.0
 */
@Data
public class UserExcelDTO implements RowIndexAware {

    @ExcelProperty("校区")
    private Long campusId;

    @ExcelProperty("用户账号")
    private String username;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("手机号码")
    private String phoneNumber;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("用户类型")
    private Integer userType;

    @ExcelIgnore
    private Integer rowIndex = -1;

}
