package com.wsw.fitnesssystem.user.application.service.command;

import com.wsw.fitnesssystem.data_exchange.application.dto.command.UserImportCommand;
import com.wsw.fitnesssystem.data_exchange.application.dto.result.UserImportResult;
import com.wsw.fitnesssystem.user.application.service.UserRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 用户注册服务实现（批量编排）。
 *
 * <p><b>职责：</b>预过滤（文件内重复 / 库内已存在）+ 逐行调用
 * {@link UserSingleRegistrar} 完成落库。
 *
 * <p><b>事务：</b>本类<b>不加</b> {@code @Transactional}，事务由
 * {@link UserSingleRegistrar#registerOne} 逐行控制，保证"一行失败不影响其他行"。
 *
 * @author loriyuhv
 * @version 1.0 2026/9/4 07:20
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final UserSingleRegistrar userSingleRegistrar;

    @Override
    public Long registerSingle(UserImportCommand data) {
        return userSingleRegistrar.registerOne(data);
    }

    @Override
    public List<UserImportResult> registerBatch(
        List<UserImportCommand> dataList, Set<String> duplicateInFile, Set<String> existingInDb) {

        List<UserImportResult> results = new ArrayList<>(dataList.size());

        for (UserImportCommand data : dataList) {
            String username = data.getUsername();

            // 1. 文件中重复 → 直接失败，不查库
            if (duplicateInFile.contains(username)) {
                results.add(failResult(data, "用户名在文件中重复"));
                continue;
            }

            // 2. 数据库中已存在 → 失败
            if (existingInDb.contains(username)) {
                results.add(failResult(data, "用户名已存在"));
                continue;
            }

            // 3. 通过 → 单行事务落库
            try {
                Long userId = userSingleRegistrar.registerOne(data);
                results.add(successResult(data, userId));
            } catch (DuplicateKeyException e) {
                results.add(failResult(data, "数据重复: 该记录已存在（用户名或唯一键冲突）"));
                log.error("Registration failed: username={}, row={}", username, data.getRowIndex(), e);
            } catch (DataIntegrityViolationException e) {
                String cause = e.getMostSpecificCause().getMessage();
                String friendlyMsg = cause.length() > 30 ? cause.substring(0, 30) + "..." : cause;
                results.add(failResult(data, "数据格式异常: " + friendlyMsg));
                log.error("Registration failed: username={}, row={}", username, data.getRowIndex(), e);
            } catch (Exception e) {
                results.add(failResult(data, "系统异常: " + e.getMessage()));
                log.error("Registration failed: username={}, row={}", username, data.getRowIndex(), e);
            }
        }

        log.info("Batch registration completed: total={}, success={}, fail={}",
            dataList.size(),
            results.stream().filter(UserImportResult::isSuccess).count(),
            results.stream().filter(r -> !r.isSuccess()).count());

        return results;
    }

    // ==================== 辅助方法 ====================

    private UserImportResult successResult(UserImportCommand data, Long userId) {
        return UserImportResult.builder()
            .rowIndex(data.getRowIndexOrDefault())
            .username(data.getUsername())
            .success(true)
            .userId(userId)
            .build();
    }

    private UserImportResult failResult(UserImportCommand data, String reason) {
        return UserImportResult.builder()
            .rowIndex(data.getRowIndexOrDefault())
            .username(data.getUsername())
            .success(false)
            .errorMessage(reason)
            .rowData(buildRowData(data))
            .build();
    }

    private List<String> buildRowData(UserImportCommand data) {
        return List.of(
            Objects.toString(data.getCampusId(), ""),
            Objects.toString(data.getUsername(), ""),
            "******",
            Objects.toString(data.getNickname(), ""),
            Objects.toString(data.getPhoneNumber(), ""),
            Objects.toString(data.getEmail(), ""),
            Objects.toString(data.getUserType(), ""),
            Objects.toString(data.getGender(), ""),
            Objects.toString(data.getBirthDate(), ""),
            Objects.toString(data.getAvatarUrl(), ""),
            Objects.toString(data.getAddress(), ""),
            Objects.toString(data.getStudentNo(), ""),
            Objects.toString(data.getClassId(), ""),
            Objects.toString(data.getEnrollYear(), ""),
            Objects.toString(data.getMajor(), ""),
            Objects.toString(data.getIdCard(), ""),
            Objects.toString(data.getFamilyAddress(), ""),
            Objects.toString(data.getTeacherNo(), "")
        );
    }

}
