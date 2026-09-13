package com.wsw.fitnesssystem.data_exchange.application.dto.upload;

import java.io.IOException;
import java.io.InputStream;

/**
 * 上传文件（应用层抽象）。
 * <p>屏蔽具体 Web 框架类型（如 Spring 的 {@code MultipartFile}），
 * 使应用层与接口层解耦。可由接口层从 {@code MultipartFile} 适配而来，
 * 也可由 CLI / MQ 等其他入口直接构造。</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/14 01:35
 * @since 1.0
 */
public interface UploadedFile {

    /**
     * 原始文件名（含扩展名）。
     */
    String getOriginalFilename();

    /**
     * 文件字节数。
     * <p>用于大小校验，避免为读取大小而将文件全量加载进内存。</p>
     */
    long getSize();

    /**
     * 打开一个新的输入流。
     * <p>每次调用返回独立实例，允许重复读取；调用方负责关闭。</p>
     */
    InputStream openInputStream() throws IOException;

}
