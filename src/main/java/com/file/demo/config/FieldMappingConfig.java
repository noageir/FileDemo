package com.file.demo.config;

import com.file.demo.service.ValueValidator;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class FieldMappingConfig {
    /**
     * 目标字段名称
     */
    private String targetField;

    /**
     * Excel表头关键字（支持多个，忽略大小写匹配）
     */
    private List<String> headerKeys;

    /**
     * 是否必填
     */
    private boolean required = false;

    /**
     * 默认值
     */
    private String defaultValue = "";

    /**
     * 校验器
     */
    private ValueValidator validator;

    /**
     * 优先级编号
     */
    private int priorityNumber;

    /**
     * 表头名称
     */
    private String titleName;

    /**
     * 字段描述
     */
    private String description;

    public FieldMappingConfig(String targetField, String... headerKeys) {
        this.targetField = targetField;
        this.headerKeys = Arrays.asList(headerKeys);
    }

    public FieldMappingConfig(String targetField, List<String> headerKeys) {
        this.targetField = targetField;
        this.headerKeys = headerKeys;
    }

    // 链式调用方法
    public FieldMappingConfig headerKeys(String... keys) {
        this.headerKeys = Arrays.asList(keys);
        return this;
    }

    public FieldMappingConfig priority(int number){
        this.priorityNumber = number;
        return this;
    }

    public FieldMappingConfig titleName(String titleName){
        this.titleName = titleName;
        return this;
    }

    public FieldMappingConfig required(boolean required) {
        this.required = required;
        return this;
    }

    public FieldMappingConfig defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public FieldMappingConfig validator(ValueValidator validator) {
        this.validator = validator;
        return this;
    }

    public FieldMappingConfig description(String description) {
        this.description = description;
        return this;
    }
}
