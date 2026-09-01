package com.wsw.fitnesssystem.handle_excel.biz.user;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;
import com.wsw.fitnesssystem.handle_excel.core.port.UserImportPort;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
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
 *   <li>数据转换（convert）：UserExcelDTO → UserImportData</li>
 *   <li>调用导入端口（persist）：通过 UserImportPort 将数据传递给 user 模块</li>
 * </ul>
 *
 * <p><b>模块解耦：</b></p>
 * <ul>
 *   <li>不依赖 user 模块的任何实体类（如 UserPo）</li>
 *   <li>只依赖自己定义的 DTO（UserImportData）和 Port 接口（UserImportPort）</li>
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
public class UserImportAdapter implements ImportAdapter<UserExcelDTO, UserImportData> {

    private final UserImportPort userImportPort;

    @Override
    public String getBizType() {
        return ExcelBizTypeEnum.USER_IMPORT.getCode();
    }

    @Override
    public Class<UserExcelDTO> getDtoClass() {
        return UserExcelDTO.class;
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
    public List<UserExcelDTO> validate(List<UserExcelDTO> batch) {
        ErrorCollector collector = ErrorCollectorHolder.get();
        List<UserExcelDTO> validList = new ArrayList<>();

        for (UserExcelDTO dto : batch) {
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

            if (trimmedUsername.length() > ExcelConstants.USERNAME_MAX_LENGTH) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "用户账号长度超过限制（最大 " + ExcelConstants.USERNAME_MAX_LENGTH + " 个字符）"
                );
                continue;
            }

            // ========== 3. 密码校验（必填） ==========
            if (StringUtils.isBlank(dto.getPassword())
                || dto.getPassword().length() < ExcelConstants.PASSWORD_MIN_LENGTH) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "密码必须至少 " + ExcelConstants.PASSWORD_MIN_LENGTH + " 位字符"
                );
                continue;
            }

            // ========== 4. 昵称校验（选填，有长度限制） ==========
            String nickname = Objects.toString(dto.getNickname(), "").trim();
            dto.setNickname(nickname);

            if (nickname.length() > ExcelConstants.NICKNAME_MAX_LENGTH) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "昵称超过最大长度限制（最大 " + ExcelConstants.NICKNAME_MAX_LENGTH + " 个字符）"
                );
                continue;
            }

            // ========== 5. 手机号码校验（选填，填了必须合法） ==========
            String phone = Objects.toString(dto.getPhoneNumber(), "").trim();
            dto.setPhoneNumber(phone);

            if (!phone.isEmpty() && !phone.matches(ExcelConstants.PHONE_REGEX)) {
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

            if (!email.isEmpty() && !email.matches(ExcelConstants.EMAIL_REGEX)) {
                collector.addError(
                    rowIndex,
                    buildRowData(dto),
                    "邮箱格式不正确（仅支持 @163.com、@126.com、@qq.com、@gmail.com）"
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
    // 2. 数据转换：UserExcelDTO → UserImportData
    // ============================================================

    @Override
    public List<UserImportData> convert(List<UserExcelDTO> dtoList) {
        List<UserImportData> dataList = new ArrayList<>();

        for (UserExcelDTO dto : dtoList) {
            UserImportData data = UserImportData.builder()
                .campusId(dto.getCampusId())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .nickname(dto.getNickname())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .userType(dto.getUserType())
                .rowIndex(dto.getRowIndex())
                .build();

            // 注意：当前 UserExcelDTO 还没有学生/教师特有字段
            // 未来扩展时，可以从 dto 中获取 studentNo, classCode 等字段

            dataList.add(data);
        }

        return dataList;
    }

    // ============================================================
    // 3. 持久化（调用 Port，由 user 模块实现）
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int persist(List<UserImportData> entities) {
        if (entities == null || entities.isEmpty()) return 0;

        ErrorCollector collector = ErrorCollectorHolder.get();

        // 调用 Port 接口（由 user 模块的 LocalUserImportAdapter 实现）
        List<UserImportResult> results = userImportPort.batchRegister(entities);

        int successCount = 0;
        for (UserImportResult result : results) {
            if (result.isSuccess()) {
                successCount++;
            } else {
                // 根据行号收集错误（User 聚合根保持纯净，rowIndex 不进入领域模型）
                int rowIndex = result.getRowIndex() != null ? result.getRowIndex() : -1;
                collector.addError(rowIndex, List.of(result.getUsername()), result.getErrorMessage());
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
        return List.of("校区", "用户账号", "密码", "昵称", "手机号码", "邮箱", "用户类型");
    }

    // ============================================================
    // 5. 辅助方法
    // ============================================================

    /**
     * 构建行数据（用于错误 Excel）
     */
    private List<String> buildRowData(UserExcelDTO dto) {
        return List.of(
            Objects.toString(dto.getCampusId(), ""),
            Objects.toString(dto.getUsername(), ""),
            Objects.toString(dto.getPassword(), ""),
            Objects.toString(dto.getNickname(), ""),
            Objects.toString(dto.getPhoneNumber(), ""),
            Objects.toString(dto.getEmail(), ""),
            Objects.toString(dto.getUserType(), "")
        );
    }

}
