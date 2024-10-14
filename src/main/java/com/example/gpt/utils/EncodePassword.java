package com.example.gpt.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class EncodePassword {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验密码
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 如果密码匹配返回 true，否则返回 false
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
