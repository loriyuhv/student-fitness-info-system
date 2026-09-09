package com.wsw.fitnesssystem.data_exchange.application.dto.record;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.data_exchange.application.plugin.UserImportPlugin;
import com.wsw.fitnesssystem.data_exchange.application.dto.RowIndexAware;
import lombok.Data;

/**
 * 用户导入记录（文件行映射）。
 * <p>
 * 对应 Excel/CSV 文件中的一行用户数据，承载原始解析结果。
 * 通过 {@link UserImportPlugin} 转换为 {@link UserImportCommand}
 * 传递给 user 模块。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:45
 * @since 1.0
 */
@Data
public class UserImportRecord implements RowIndexAware {

    // ==================== 用户核心字段 ====================

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

    // ==================== 用户扩展信息（user_profile） ====================

    /** 0-未知 1-男 2-女 */
    @ExcelProperty("性别")
    private Integer gender;

    /** 格式：yyyy-MM-dd */
    @ExcelProperty("出生日期")
    private String birthDate;

    @ExcelProperty("头像URL")
    private String avatarUrl;

    @ExcelProperty("联系地址")
    private String address;

    // ==================== 学生特有字段 ====================

    /** 学生必填，不填默认使用 username */
    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("班级ID")
    private Long classId;

    @ExcelProperty("入学年份")
    private Integer enrollYear;

    @ExcelProperty("专业")
    private String major;

    @ExcelProperty("身份证号")
    private String idCard;

    @ExcelProperty("家庭地址")
    private String familyAddress;

    // ==================== 教师特有字段 ====================

    /** 教师必填，不填默认使用 username */
    @ExcelProperty("教师工号")
    private String teacherNo;

    // ==================== 行号（由监听器注入） ====================

    @ExcelIgnore
    private Integer rowIndex = -1;

}
