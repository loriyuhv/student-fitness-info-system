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
import java.util.Map;
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
            // 1. 校验用户账号不能为空
            if (StringUtils.isBlank(dto.getUsername())) {
                collector.addError(
                    dto.getRowIndex(),
                    List.of(
                        "",
                        Objects.toString(dto.getPassword(), ""),
                        Objects.toString(dto.getNickname(), "")
                    ),
                    "用户账号不能为空"
                );
                continue;
            }

            String trimmedUsername = dto.getUsername().trim();
            dto.setUsername(trimmedUsername);

            // 2. 校验用户名长度
            if (trimmedUsername.length() > ExcelConstants.USERNAME_MAX_LENGTH) {
                collector.addError(
                    dto.getRowIndex(),
                    List.of(
                        dto.getUsername(),
                        Objects.toString(dto.getPassword(), ""),
                        Objects.toString(dto.getNickname(), "")
                    ),
                    "用户账号长度超过限制（最大 " + ExcelConstants.USERNAME_MAX_LENGTH + " 个字符）"
                );
                continue;
            }

            // 3. 密码格式校验 密码不能为空并且长度不能小于6
            if (StringUtils.isBlank(dto.getPassword())
                || dto.getPassword().length() < ExcelConstants.PASSWORD_MIN_LENGTH) {

                collector.addError(
                    dto.getRowIndex(),
                    List.of(
                        trimmedUsername,
                        Objects.toString(dto.getPassword(), ""),
                        Objects.toString(dto.getNickname(), "")
                    ),
                    "密码必须至少" + ExcelConstants.PASSWORD_MIN_LENGTH + "位字符"
                );
                continue;

            }

            // 校验通过，加入成功列表
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
        return parallelConvertExecutor.execute(domainUsers, userAssembler::toEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persist(List<SysUser> entities) {
        if (entities == null || entities.isEmpty()) return;

        ErrorCollector collector = ErrorCollectorHolder.get();
        // 内部再分片，防止 SQL 过长
        batchPersistHelper.safeBatchInsert(
            entities,
            userMapper::batchInsert,  // 批量插入
            userMapper::insert, // 单条插入（降级时使用）
            ExcelConstants.DB_BATCH_SIZE,
            collector
        );
        // log.debug("用户批量插入完成，共 {} 条", entities.size());
    }

    @Override
    public List<String> getHeaders() {
        return List.of("用户账号", "密码", "昵称");
    }

    @Override
    public List<String> toRowData(SysUser entity) {
        return List.of(entity.getUsername(), entity.getPassword(), entity.getNickname());
    }

    @Override
    public Map<SysUser, Integer> getRowIndexMap(List<SysUser> entities) {
        // 如果需要精确行号，在 convert 时保存映射
        // 由于我们通过 User 的 rowIndex 已保存，可在 UserAssembler 中赋值
        return Map.of();
    }

}
