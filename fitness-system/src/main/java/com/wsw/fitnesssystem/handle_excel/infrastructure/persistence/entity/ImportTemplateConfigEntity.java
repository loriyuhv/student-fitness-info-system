package com.wsw.fitnesssystem.handle_excel.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/1 02:16
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "excel_template_config", autoResultMap = true)
public class ImportTemplateConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("biz_type")
    private String bizType;

    @TableField("file_name")
    private String fileName;

    @TableField("sheet_name")
    private String sheetName;

    @TableField(value = "headers", typeHandler = JacksonTypeHandler.class)
    private List<String> headers;

    @TableField(value = "rules", typeHandler = JacksonTypeHandler.class)
    private List<String> rules;

    @TableField(value = "examples", typeHandler = JacksonTypeHandler.class)
    private List<List<String>> examples;

    @TableField("version")
    @Version
    private Integer version;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /** 创建人ID：只在插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人ID：插入和更新时都填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 创建时间：只在插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间：插入和更新时都填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
