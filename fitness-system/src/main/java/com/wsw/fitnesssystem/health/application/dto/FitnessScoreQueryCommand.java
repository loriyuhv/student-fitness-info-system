package com.wsw.fitnesssystem.health.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 20:40
 * @since 1.0
 */
@Data
@Builder
public class FitnessScoreQueryCommand {

    /** 性别：1-男 2-女 */
    private Integer gender;

    /** 年级：1-大一 2-大二 3-大三 4-大四 */
    private Integer grade;

    /** 项目编码 */
    private String itemCode;

    /** 原始成绩 */
    private Double rawValue;

}
