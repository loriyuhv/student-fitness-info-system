package com.wsw.fitnesssystem.handle_excel.core.port;

/**
 * 文件导入防重锁端口
 * <p>基于文件内容指纹（MD5）防止同一文件被重复提交导入</p>
 * <p>Core 层只声明契约，不关心 Redis / 数据库 / 本地内存</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:46
 * @since 1.0
 */
public interface ImportFileLockPort {

    /**
     * 尝试获取文件导入锁
     * @param fileMd5 文件 MD5 指纹
     * @param taskId 当前任务ID（作为锁的 value，便于排查）
     * @return true 获取成功；false 该文件正在导入中
     */
    boolean tryLock(String fileMd5, String taskId);

    /**
     * 释放文件导入锁
     * <p>任务完成后必须调用，避免死锁</p>
     *
     * @param fileMd5 文件 MD5 指纹
     */
    void releaseLock(String fileMd5);

}
