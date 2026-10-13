package com.file.demo.dto;

import com.file.demo.config.FieldMappingConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelContext {
    private static final ThreadLocal<ExcelContext> CONTEXT = ThreadLocal.withInitial(ExcelContext::new);

    private String[] headers;
    private Map<String, Integer> fieldIndexMap;
    private Map<Integer, String> unmatchedHeaders;
    private List<FieldMappingConfig> fieldConfigs;
    private String fileName;
    private String extensionName;

    public static ExcelContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}
