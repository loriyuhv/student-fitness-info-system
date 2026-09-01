package com.wsw.fitnesssystem.user.application.dto.result;

import com.wsw.fitnesssystem.user.application.service.UserInfoQueryService;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 用户信息查询结果（Application 层纯数据对象）
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>作为 {@link UserInfoQueryService} 的返回值</li>
 *   <li>包含用户核心资料，不含权限列表（权限由授权模块单独提供）</li>
 *   <li><b>严禁</b>添加任何 Web/JSON 序列化注解</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/26 16:02
 * @since 1.0
 */
@Data
@Builder
public class UserInfoResult {

    private Long userId;
    private Long campusId;
    private String username;
    private String nickname;
    private String phoneNumber;
    private String email;
    private String remark;
    /** 0-管理员, 1-教师, 2-学生 */
    private Integer userType;
    private Set<String> permissions;

}
