package com.wsw.fitnesssystem.handle_excel.biz.user;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollector;
import com.wsw.fitnesssystem.handle_excel.core.collector.ErrorCollectorHolder;
import com.wsw.fitnesssystem.handle_excel.core.executor.ParallelConvertExecutor;
import com.wsw.fitnesssystem.handle_excel.core.helper.BatchPersistHelper;
import com.wsw.fitnesssystem.handle_excel.domain.enums.ExcelBizTypeEnum;
import com.wsw.fitnesssystem.handle_excel.domain.model.User;
import com.wsw.fitnesssystem.handle_excel.domain.service.UserImportDomainService;
import com.wsw.fitnesssystem.handle_excel.infrastructure.config.ExcelConstants;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.assembler.UserAssembler;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.entity.SysUser;
import com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.db.mapper.ExcelSysUserMapper;
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
 * <p>职责边界：</p>
 * <li>1. 轻量过滤（空用户名等明显非法数据）</li>
 * <li>2. 调用 DomainService 完成业务校验 + 查重 + 领域转换</li>
 * <li>3. 通过 Assembler 完成 Domain → Entity 的技术转换</li>
 * <li>4. 批量持久化</li>
 * <p>不再包含业务规则（如密码默认值、昵称默认值、长度校验），这些已下沉到 {@link User}</p>
 * @author loriyuhv
 * @version 1.0 2026/8/21 15:48
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserImportAdapter implements ImportAdapter<UserExcelDTO, SysUser> {

    private final UserAssembler userAssembler;
    private final ExcelSysUserMapper userMapper;
    private final UserImportDomainService domainService;
    private final BatchPersistHelper batchPersistHelper;
    private final ParallelConvertExecutor parallelConvertExecutor;

    @Override
    public String getBizType() {
        return ExcelBizTypeEnum.USER_IMPORT.getCode();
    }

    @Override
    public Class<UserExcelDTO> getDtoClass() {
        return UserExcelDTO.class;
    }

    /**
     * <p>适配器层只做格式校验：只负责必填、长度、格式等基础校验</p>
     * <p>复杂的业务查重、格式规则收敛到 DomainService</p>
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

            // 所有校验通过
            validList.add(dto);
        }

        return validList;
    }

    @Override
    public List<SysUser> convert(List<UserExcelDTO> dtoList) {
        // Step 1: 领域层完成业务校验 + 查重 + 领域转换
        List<User> domainUsers = domainService.validateAndConvert(dtoList);

        // Step 2: 技术转换：Domain → Entity（含密码加密、默认值填充等技术细节）
        // return userAssembler.toEntityList(domainUsers);
        return parallelConvertExecutor.execute(domainUsers, userAssembler::toEntity, User::getRowIndex);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int persist(List<SysUser> entities) {
        if (entities == null || entities.isEmpty()) return 0;

        ErrorCollector collector = ErrorCollectorHolder.get();
        // 内部再分片，防止 SQL 过长
        return batchPersistHelper.safeBatchInsert(
            entities,
            userMapper::batchInsert,  // 批量插入
            userMapper::insert, // 单条插入（降级时使用）
            ExcelConstants.DB_BATCH_SIZE,
            collector,
            SysUser::getRowIndex
        );
    }

    @Override
    public List<String> getHeaders() {
        return List.of("校区", "用户账号", "密码", "昵称", "手机号码", "邮箱", "用户类型");
    }

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
