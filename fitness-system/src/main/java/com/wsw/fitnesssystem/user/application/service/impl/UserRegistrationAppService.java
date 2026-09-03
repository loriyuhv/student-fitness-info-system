package com.wsw.fitnesssystem.user.application.service.impl;

import com.wsw.fitnesssystem.handle_excel.core.model.UserImportData;
import com.wsw.fitnesssystem.handle_excel.core.model.UserImportResult;
import com.wsw.fitnesssystem.user.application.service.UserRegisterService;
import com.wsw.fitnesssystem.user.domain.port.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 用户注册应用服务
 * <p>处理用户注册相关的业务用例，包括 Excel 批量导入注册</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 13:13
 * @since 1.0
 */
@Slf4j
@Service
public class UserRegistrationAppService {

    private final Executor computeExecutor;
    private final UserRepository userRepository;
    private final UserRegisterService userRegisterService;
    private final PasswordEncryptorPort passwordEncryptorPort;

    public UserRegistrationAppService(
        UserRepository userRepository,
        UserRegisterService userRegisterService,
        PasswordEncryptorPort passwordEncryptorPort,
        @Qualifier("computeExecutor") Executor computeExecutor
    ) {
        this.userRepository = userRepository;
        this.computeExecutor = computeExecutor;
        this.userRegisterService = userRegisterService;
        this.passwordEncryptorPort = passwordEncryptorPort;
    }

    /**
     * 批量注册用户（Excel 导入入口）
     * 无事务：查重、加密不需要事务
     * <p>处理流程：</p>
     * <ol>
     *   <li>按用户名分组，标记文件中重复的行（只保留第一条，其余直接失败）</li>
     *   <li>批量查数据库，获取已存在的用户名</li>
     *   <li>调用doSave逐行处理(事务）：文件中重复 → 失败；数据库中已存在 → 失败；通过 → 创建并保存</li>
     * </ol>
     *
     * @param dataList 用户导入数据列表
     * @return 每条数据的处理结果（含行号、成功/失败状态、错误原因）
     */
    public List<UserImportResult> batchRegister(List<UserImportData> dataList) {

        if (dataList == null || dataList.isEmpty()) {
            return List.of();
        }

        // ==================== 第一步：检测文件中重复的用户名 ====================
        // 按用户名分组，用于识别重复
        Map<String, List<UserImportData>> groupedByUsername = dataList.stream()
            .collect(Collectors.groupingBy(UserImportData::getUsername));

        // 收集每个用户名第一次出现的数据（用于后续处理）
        List<UserImportData> firstOccurrenceList = new ArrayList<>();
        // 记录文件中重复的用户名（仅保留除第一条外的其余行）
        Set<String> duplicateInFile = new HashSet<>();

        for (Map.Entry<String, List<UserImportData>> entry : groupedByUsername.entrySet()) {
            List<UserImportData> list = entry.getValue();
            // 第一条保留（用于后续处理）
            firstOccurrenceList.add(list.get(0));
            // 其余行标记为文件中重复
            if (list.size() > 1) {
                for (int i = 1; i < list.size(); i++) {
                    duplicateInFile.add(list.get(i).getUsername());
                }
            }
        }

        // ==================== 第二步：批量查数据库（只查每个用户名第一次出现的） ====================
        List<String> usernamesToCheck = firstOccurrenceList.stream()
            .map(UserImportData::getUsername)
            .toList();
        Set<String> existingInDb = userRepository.findExistingUsernames(usernamesToCheck);

        // 只对通过查重的数据加密
        List<UserImportData> needEncrypt = dataList.stream()
            .filter(data -> !duplicateInFile.contains(data.getUsername()))
            .filter(data -> !existingInDb.contains(data.getUsername()))
            .toList();

        // 并行加密，结果按原顺序保存
        List<CompletableFuture<Void>> futures = needEncrypt.stream()
            .map(data -> CompletableFuture.runAsync(() -> {
                String encoded = passwordEncryptorPort.encode(data.getPassword());
                data.setPassword(encoded);  // ⚠️ 需要给 UserImportData 加 setPassword 方法
            }, computeExecutor))
            .toList();

        futures.forEach(CompletableFuture::join);  // 等待全部加密完成

        // ==================== 第三步：逐行处理 ====================
        return userRegisterService.registerBatch(dataList, duplicateInFile, existingInDb);
    }

}
