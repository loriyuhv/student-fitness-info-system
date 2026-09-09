package com.wsw.fitnesssystem.data_exchange.application.port.output;

import com.wsw.fitnesssystem.shared.exception.BizException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件存储端口（输出端口）。
 * <p>
 * 定义文件上传、转存、清理等操作契约，由基础设施层实现。
 * </p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/9 14:55
 * @since 1.0
 */
public interface FileStoragePort {


    /**
     * 转存上传的文件到临时目录。
     *
     * @param file   上传的文件
     * @param taskId 任务 ID（用于隔离不同任务的临时目录）
     * @return 转存后的临时文件对象
     * @throws BizException 转存失败时抛出
     */
    File saveTempFile(MultipartFile file, String taskId);

    /**
     * 计算文件的 MD5。
     *
     * @param file 文件
     * @return MD5 十六进制字符串
     * @throws BizException 计算失败时抛出
     */
    String computeFileMd5(File file);

    /**
     * 清理临时文件及其父目录。
     *
     * @param file 临时文件
     */
    void cleanup(File file);

}
