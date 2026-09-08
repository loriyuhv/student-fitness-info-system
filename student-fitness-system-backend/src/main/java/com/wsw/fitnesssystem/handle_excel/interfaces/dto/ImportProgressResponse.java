package com.wsw.fitnesssystem.handle_excel.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 导入进度 DTO
 * 用于前端展示导入状态
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 16:09
 * @since 1.0
 */
@Getter
@Builder
public class ImportProgressResponse {

    /**
     * 总数据条数
     */
    private int total;

    /**
     * 已处理条数（成功 + 失败）
     */
    private int processed;

    /**
     * 成功条数
     */
    @JsonProperty("success_count")
    private int successCount;

    /**
     * 失败条数
     */
    @JsonProperty("fail_count")
    private int failCount;

    /**
     * 状态：INIT / PROCESSING / FINISHED / PARTIAL / FAILED / NOT_FOUND
     */
    private String status;

    /**
     * 错误信息摘要（最多前3条）
     */
    @JsonProperty("error_msg")
    private String errorMsg;

    /**
     * 是否有错误文件可下载
     */
    @JsonProperty("error_file_exists")
    private boolean errorFileExists;

    /**
     * 进度百分比
     */
    private int percent;

    /**
     * 是否已完成
     */
    private boolean completed;

    /**
     * 是否失败
     */
    private boolean failed;

    /**
     * 是否运行
     */
    private boolean running;

}
