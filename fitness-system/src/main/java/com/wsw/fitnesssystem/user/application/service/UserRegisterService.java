package com.wsw.fitnesssystem.user.application.service;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;

import java.util.List;
import java.util.Set;

/**
 * 用户注册服务（独立于导入逻辑）
 * <p>支持：单条注册、批量注册、导入注册</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/4 07:19
 * @since 1.0
 */
public interface UserRegisterService {

    /**
     * 单条用户注册（事务内）
     * @param data 用户注册数据
     * @return 新生成的 userId
     */
    Long registerSingle(UserImportData data);

    /**
     * 批量用户注册（事务内，逐行独立事务）
     * @param dataList 用户注册数据列表
     * @param duplicateInFile 文件中重复的用户名
     * @param existingInDb 数据库中已存在的用户名
     * @return 每条数据的导入结果
     */
    List<UserImportResult> registerBatch(
        List<UserImportData> dataList,
        Set<String> duplicateInFile,
        Set<String> existingInDb
    );

}
