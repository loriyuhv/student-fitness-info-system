package com.wsw.fitnesssystem.data_exchange.application.plugin;

import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorCollector;
import com.wsw.fitnesssystem.data_exchange.application.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.data_exchange.application.config.ImportApplicationProperties;
import com.wsw.fitnesssystem.data_exchange.application.dto.record.UserImportRecord;
import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.data_exchange.application.dto.result.UserImportResult;
import com.wsw.fitnesssystem.data_exchange.application.port.output.UserProvisioningPort;
import com.wsw.fitnesssystem.data_exchange.application.enums.ImportBizType;
import com.wsw.fitnesssystem.shared.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户导入适配器 — 技术适配层 支持并行密码加密
 *
 * <p>职责边界：</p>
 * <ul>
 *   <li>格式校验（validate）：必填、长度、格式（手机号/邮箱）</li>
 *   <li>数据转换（convert）：UserImportRecord → UserImportCommand</li>
 *   <li>调用导入端口（persist）：通过 UserProvisioningPort 将数据传递给 user 模块</li>
 * </ul>
 *
 * <p><b>模块解耦：</b></p>
 * <ul>
 *   <li>不依赖 user 模块的任何实体类（如 UserPo）</li>
 *   <li>只依赖自己定义的 DTO（UserImportCommand）和 Port 接口（UserProvisioningPort）</li>
 *   <li>为微服务拆分预留零成本切换路径</li>
 * </ul>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:48
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserImportPlugin implements ImportPlugin<UserImportRecord, UserImportCommand> {

    private final UserProvisioningPort userProvisioningPort;
    private final ImportApplicationProperties appProperties;

    @Override
    public String getBizType() {
        return ImportBizType.USER_IMPORT.getCode();
    }

    @Override
    public Class<UserImportRecord> getDtoClass() {
        return UserImportRecord.class;
    }

    @Override
    public int getBatchSize() {
        return appProperties.getBatch().getDefaultSize();
    }

    // ============================================================
    // 1. 格式校验（必填 + 长度 + 格式）
    // ============================================================

    /**
     * <p>适配器层只做格式校验：只负责必填、长度、格式等基础校验</p>
     * <p>复杂的业务查重、格式规则收敛到 user模块</p>
     * <p>但必须记录被剔除的行，供错误 Excel 生成使用</p>
     *
     * @param batch 原始 DTO 批次
     * @return 格式校验通过的 DTO 列表
     */
    @Override
    public List<UserImportRecord> validate(List<UserImportRecord> batch) {
        ErrorCollector collector = ErrorCollectorHolder.get();
        List<UserImportRecord> validList = new ArrayList<>();

        for (UserImportRecord dto : batch) {
            int rowIndex = dto.getRowIndex() != null ? dto.getRowIndex() : -1;

            // ========== 1. 校区校验（必填） ==========
            if (dto.getCampusId() == null) {
                collector.addError(rowIndex, buildRowData(dto), "校区不能为空");
                continue;
            }

            // ========== 2. 用户账号校验（必填） ==========
            if (StringUtils.isBlank(dto.getUsername())) {
                collector.addError(rowIndex, buildRowData(dto), "用户账号不能为空");
                continue;
            }

            String trimmedUsername = dto.getUsername().trim();
            dto.setUsername(trimmedUsername);

            if (trimmedUsername.length() > appProperties.getValidation().getUsernameMaxLength()) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "用户账号长度超过限制（最大%s个字符）".formatted(
                        appProperties.getValidation().getUsernameMaxLength()
                    )
                );
                continue;
            }

            // ========== 3. 密码校验（必填） ==========
            if (StringUtils.isBlank(dto.getPassword())
                || dto.getPassword().length() < appProperties.getValidation().getPasswordMinLength()) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "密码必须至少%s位字符".formatted(
                        appProperties.getValidation().getPasswordMinLength()
                    )
                );
                continue;
            }

            // ========== 4. 昵称校验（选填，有长度限制） ==========
            String nickname = Objects.toString(dto.getNickname(), "").trim();
            dto.setNickname(nickname);

            if (nickname.length() > appProperties.getValidation().getNicknameMaxLength()) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "昵称超过最大长度限制（最大 %s 个字符）".formatted(
                        appProperties.getValidation().getNicknameMaxLength()
                    )
                );
                continue;
            }

            // ========== 5. 手机号码校验（选填，填了必须合法） ==========
            String phone = Objects.toString(dto.getPhoneNumber(), "").trim();
            dto.setPhoneNumber(phone);

            if (!phone.isEmpty() && !ValidationUtils.isPhone(phone)) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "手机号码格式不正确（需为 11 位中国手机号）"
                );
                continue;
            }

            // ========== 6. 邮箱校验（选填，填了必须合法） ==========
            String email = Objects.toString(dto.getEmail(), "").trim().toLowerCase();
            dto.setEmail(email);

            if (!email.isEmpty() && !ValidationUtils.isEmail(email)) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "邮箱格式不正确"
                );
                continue;
            }

            // ========== 7. 用户类型校验（必填） ==========
            if (dto.getUserType() == null) {
                collector.addError(rowIndex, buildRowData(dto), "用户类型不能为空");
                continue;
            }

            if (dto.getUserType() < 0 || dto.getUserType() > 2) {
                collector.addError(rowIndex, buildRowData(dto),
                    "用户类型不正确，只能为 0（管理员）、1（教师）、2（学生）");
                continue;
            }

            // 所有校验通过
            validList.add(dto);
        }

        return validList;
    }

    // ============================================================
    // 2. 数据转换：UserImportRecord → UserImportCommand
    // ============================================================

    @Override
    public List<UserImportCommand> convert(List<UserImportRecord> dtoList) {
        List<UserImportCommand> dataList = new ArrayList<>();

        for (UserImportRecord dto : dtoList) {
            UserImportCommand data = UserImportCommand.builder()
                .campusId(dto.getCampusId())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .userType(dto.getUserType())
                .rowIndex(dto.getRowIndex())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .address(dto.getAddress())
                .studentNo(dto.getStudentNo())
                .classId(dto.getClassId())
                .enrollYear(dto.getEnrollYear())
                .major(dto.getMajor())
                .idCard(dto.getIdCard())
                .familyAddress(dto.getFamilyAddress())
                .teacherNo(dto.getTeacherNo())
                .build();

            dataList.add(data);
        }

        return dataList;
    }

    // ============================================================
    // 3. 持久化（调用 Port，由 user 模块实现）
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int persist(List<UserImportCommand> entities) {
        if (entities == null || entities.isEmpty()) return 0;

        ErrorCollector collector = ErrorCollectorHolder.get();

        // 调用 Port 接口（由 user 模块的 UserProvisioningLocalAdapter 实现）
        List<UserImportResult> results = userProvisioningPort.importUsers(entities);

        int successCount = 0;
        for (UserImportResult result : results) {
            if (result.isSuccess()) {
                successCount++;
            } else {
                // 根据行号收集错误（User 聚合根保持纯净，rowIndex 不进入领域模型）
                int rowIndex = result.getRowIndex() != null ? result.getRowIndex() : -1;
                collector.addError(rowIndex, result.getRowData(), result.getErrorMessage());
            }
        }

        log.info("用户导入完成: 总数={}, 成功={}, 失败={}",
            entities.size(), successCount, entities.size() - successCount);

        return successCount;
    }


    // ============================================================
    // 4. 元数据
    // ============================================================
    @Override
    public List<String> getHeaders() {
        return List.of(
            "校区",
            "用户账号",
            "密码",
            "昵称",
            "手机号码",
            "邮箱",
            "用户类型",
            "性别",
            "出生日期",
            "头像URL",
            "联系地址",
            "学号",
            "班级ID",
            "入学年份",
            "专业",
            "身份证号",
            "家庭地址",
            "教师工号"
        );
    }

    // ============================================================
    // 5. 辅助方法
    // ============================================================

    /**
     * 构建行数据（用于错误 Excel）
     */
    private List<String> buildRowData(UserImportRecord dto) {
        return List.of(
            Objects.toString(dto.getCampusId(), ""),
            Objects.toString(dto.getUsername(), ""),
            Objects.toString("******", ""),
            Objects.toString(dto.getNickname(), ""),
            Objects.toString(dto.getPhoneNumber(), ""),
            Objects.toString(dto.getEmail(), ""),
            Objects.toString(dto.getUserType(), ""),
            Objects.toString(dto.getGender(), ""),
            Objects.toString(dto.getBirthDate(), ""),
            Objects.toString(dto.getAvatarUrl(), ""),
            Objects.toString(dto.getAddress(), ""),
            Objects.toString(dto.getStudentNo(), ""),
            Objects.toString(dto.getClassId(), ""),
            Objects.toString(dto.getEnrollYear(), ""),
            Objects.toString(dto.getMajor(), ""),
            Objects.toString(dto.getIdCard(), ""),
            Objects.toString(dto.getFamilyAddress(), ""),
            Objects.toString(dto.getTeacherNo(), "")
        );
    }

}
