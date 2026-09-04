package com.wsw.fitnesssystem.health.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author loriyuhv
 * @version 1.0 2026/9/4 23:49
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class BmiResult {

    private Integer score;

    private String levelCode;

    private String levelName;

}
