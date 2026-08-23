package com.wsw.fitnesssystem.handle_excel.biz.user;

import com.wsw.fitnesssystem.handle_excel.application.dto.UserExcelDTO;
import com.wsw.fitnesssystem.handle_excel.core.adapter.ImportAdapter;
import com.wsw.fitnesssystem.handle_excel.core.executor.ParallelConvertExecutor;
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

import java.util.List;
import java.util.stream.Collectors;

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

    private final ExcelSysUserMapper userMapper;
    private final UserImportDomainService domainService;
    private final UserAssembler userAssembler;
    private final ParallelConvertExecutor parallelConvertExecutor;

    @Override
    public String getBizType() {
        return ExcelBizTypeEnum.USER_IMPORT.getCode();
    }

    @Override
    public Class<UserExcelDTO> getDtoClass() {
        return UserExcelDTO.class;
    }

    @Override
    public List<UserExcelDTO> validate(List<UserExcelDTO> batch) {
        // 适配器层只做轻量过滤：剔除明显无法解析的行（空用户名）
        // 复杂的业务查重、格式规则收敛到 DomainService
        return batch.stream()
                .filter(dto -> dto != null && StringUtils.isNotBlank(dto.getUsername()))
                .collect(Collectors.toList());
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

        // 内部再分片，防止 SQL 过长
        int batchSize = ExcelConstants.DB_BATCH_SIZE;
        for (int i = 0; i < entities.size(); i += batchSize) {
            List<SysUser> batch = entities.subList(i, Math.min(i + batchSize, entities.size()));
            userMapper.batchInsert(batch);
        }
        log.debug("用户批量插入完成，共 {} 条", entities.size());
    }
}
