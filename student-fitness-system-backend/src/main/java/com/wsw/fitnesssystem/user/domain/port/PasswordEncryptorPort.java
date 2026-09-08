package com.wsw.fitnesssystem.user.domain.port;

/**
 * 密码加密端口
 * <p>由 Infrastructure 层实现，提供密码加密能力</p>
 *
 * @author loriyuhv
 * @version 1.0 2026/9/1 18:51
 * @since 1.0
 */
public interface PasswordEncryptorPort {

    /**
     * 加密明文密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密文
     */
    String encode(String rawPassword);

}
