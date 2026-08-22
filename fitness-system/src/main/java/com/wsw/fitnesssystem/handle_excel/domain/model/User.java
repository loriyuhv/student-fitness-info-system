package com.wsw.fitnesssystem.handle_excel.domain.model;

import lombok.Data;

/**
 * 用户领域模型
 * <p>纯 POJO，不依赖任何框架注解，承载业务语义</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/3/26 15:58
 * @since 1.0
 */
@Data
public class User {
    private String username;
    private String password;
    private String nickname;
}
