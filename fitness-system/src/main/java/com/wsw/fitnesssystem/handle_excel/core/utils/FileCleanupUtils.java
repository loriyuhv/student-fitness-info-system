package com.wsw.fitnesssystem.handle_excel.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * 文件清理工具类
 * 用于统一管理导入过程中的临时文件清理
 *
 * @author loriyuhv
 * @version 1.0 2026/8/30 20:26
 * @since 1.0
 */
@Slf4j
public class FileCleanupUtils {

    private FileCleanupUtils() {
        // 私有构造器，防止实例化
    }

    /**
     * 清理临时文件及其父目录
     *
     * @param file 待清理的临时文件
     */
    public static void cleanup(File file) {
        if (file == null) {
            return;
        }

        try {
            // 1. 删除文件
            if (file.exists()) {
                FileUtils.delete(file);
                log.debug("Temp file deleted: {}", file.getAbsolutePath());
            }

            // 2. 删除父目录（如果为空）
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                FileUtils.deleteDirectory(parent);
                log.debug("Temp directory deleted: {}", parent.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Failed to clean up temp files, path={}", file.getAbsolutePath(), e);
        }
    }

}
