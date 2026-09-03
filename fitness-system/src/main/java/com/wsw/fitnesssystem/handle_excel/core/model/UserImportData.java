package com.wsw.fitnesssystem.handle_excel.core.model;

import com.wsw.fitnesssystem.handle_excel.core.port.UserImportPort;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户导入数据传输对象
 * <p>由 handle_excel 模块定义，作为 {@link UserImportPort} 的参数，
 * 将 Excel 解析后的数据传递给 user 模块进行持久化。</p>
 *
 * <p>职责如下：</p>
 * <ul>
 *   <li>只包含导入所需的最小数据集</li>
 *   <li>不依赖 user 模块的领域对象，避免模块间耦合</li>
 *   <li>为未来微服务拆分时的 Feign 调用提供序列化基础</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 12:08
 * @since 1.0
 */
@Getter
@Builder
@ToString
public class UserImportData {

    // ==================== 用户核心字段 ====================

    /** 校区ID（必填） */
    private Long campusId;

    /** 用户账号（必填，唯一） */
    private String username;

    /** 密码（必填，明文，由 user 模块负责加密） */
    @Setter
    private String password;

    /** 昵称（选填，为空时默认使用 username） */
    private String nickname;

    /** 手机号码（选填） */
    private String phoneNumber;

    /** 邮箱（选填） */
    private String email;

    /** 用户类型（必填）：0-管理员，1-教师，2-学生 */
    private Integer userType;

    /** Excel 行号（用于错误定位） */
    private Integer rowIndex;

    // ==================== 用户扩展信息（user_profile） ====================

    private Integer gender;          // 0-未知 1-男 2-女
    private String birthDate;        // 出生日期，格式：yyyy-MM-dd
    private String avatarUrl;
    private String address;

    // ==================== 学生特有字段 ====================

    /** 学号（仅学生类型时使用） 如果不传，默认用 username */
    private String studentNo;

    /** 班级编码（仅学生类型时使用，导入时只记录编码，不校验是否存在） */
    private Long classId;

    /** 专业（仅学生类型时使用） */
    private String major;

    /** 入学年份（仅学生类型时使用） */
    private Integer enrollYear;

    /** 身份证号（仅学生类型时使用） */
    private String idCard;

    /** 家庭地址 */
    private String familyAddress;

    // ==================== 教师特有字段 ====================

    /** 教师工号（仅教师类型时使用） （如果不传，默认用 username） */
    private String teacherNo;

    // ==================== 工具方法 ====================

    /**
     * 判断是否为学生
     */
    public boolean isStudent() {
        return userType != null && userType == 2;
    }

    /**
     * 判断是否为教师
     */
    public boolean isTeacher() {
        return userType != null && userType == 1;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return userType != null && userType == 0;
    }

    /**
     * 获取行号（用于错误收集，若为 null 则返回 -1）
     */
    public int getRowIndexOrDefault() {
        return rowIndex != null ? rowIndex : -1;
    }

    public String getStudentNoOrDefault() {
        return studentNo != null && !studentNo.isBlank() ? studentNo : username;
    }

    public String getTeacherNoOrDefault() {
        return teacherNo != null && !teacherNo.isBlank() ? teacherNo : username;
    }

    public Integer getGenderOrDefault() {
        return gender != null ? gender : 0;
    }

}
