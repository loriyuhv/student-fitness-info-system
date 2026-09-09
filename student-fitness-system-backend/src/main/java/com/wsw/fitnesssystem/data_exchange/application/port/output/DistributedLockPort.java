package com.wsw.fitnesssystem.data_exchange.application.port.output;

/**
 * 分布式锁端口（输出端口）。
 * <p>
 * <b>职责：</b>基于文件内容指纹（MD5）防止同一文件被重复提交导入。
 * <p>
 * <b>调用方向：</b>
 * <pre>
 * ImportSubmissionService（应用层） → DistributedLockPort（契约） → RedisDistributedLockAdapter（基础设施）
 * </pre>
 * <p>
 * <b>锁粒度：</b>文件 MD5，即相同内容的文件视为同一资源，同时只能有一个导入任务。
 *
 * @author loriyuhv
 * @version 1.0 2026/8/23 20:46
 * @since 1.0
 */
public interface DistributedLockPort {

    /**
     * 尝试获取文件锁。
     * <p>
     * 若文件未被锁定则获取成功，否则返回 {@code false}。
     * 获取锁后需在任务完成时调用 {@link #releaseLock(String, String)} 释放。
     *
     * @param fileMd5 文件 MD5（锁的唯一标识）
     * @param taskId  当前任务 ID（存入锁值，便于排查）
     * @return {@code true} 获取成功；{@code false} 文件正在被其他任务导入
     */
    boolean tryLock(String fileMd5, String taskId);

    /**
     * 释放文件锁。
     * <p>
     * 任务完成后（无论成功/失败）必须调用，避免死锁。
     * Redis TTL 作为兜底释放机制。
     * </p>
     * <p>
     * <b>持有者校验：</b>仅当锁的持有者（value 中的 taskId）与 {@code taskId} 一致时才删除，
     * 防止 TTL 过期后旧任务释放了被新任务重新获取的锁。
     *
     * @param fileMd5 文件 MD5
     * @param taskId  持有锁的任务 ID（用于持有者校验，必须与加锁时一致）
     */
    void releaseLock(String fileMd5, String taskId);

}
