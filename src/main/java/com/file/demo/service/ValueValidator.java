package com.file.demo.service;

import com.file.demo.dto.ValidationResult;

@FunctionalInterface
public interface ValueValidator {
    /**
     * 校验值
     * @param value 待校验的值
     * @param targetField 目标字段
     * @param headerKey Excel表头
     * @return 校验结果
     */
    ValidationResult validate(String value, String targetField, String headerKey);

    /**
     * 默认校验器 - 非空校验
     */
    ValueValidator NOT_EMPTY = (value, targetField, headerKey) -> {
        boolean valid = value != null && !value.trim().isEmpty();
        return ValidationResult.of(valid, valid ? null : String.format("字段[%s]不能为空", headerKey));
    };

    /**
     * IP地址校验器
     */
    ValueValidator IP_VALIDATOR = (value, targetField, headerKey) -> {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.valid();
        }
        // 简单的IP地址格式校验
        boolean valid = value.matches("^(\\d{1,3}\\.){3}\\d{1,3}$");
        return ValidationResult.of(valid, valid ? null : String.format("IP地址格式不正确: %s", value));
    };

    /**
     * 主机名校验器
     */
    ValueValidator HOSTNAME_VALIDATOR = (value, targetField, headerKey) -> {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.valid();
        }
        boolean valid = value.matches("^[a-zA-Z0-9-_.]+$");
        return ValidationResult.of(valid, valid ? null : String.format("主机名只能包含字母、数字、-、_、.: %s", value));
    };
}
