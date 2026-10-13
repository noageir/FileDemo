package com.file.demo.config;

import com.file.demo.service.ValueValidator;
import lombok.Getter;

import java.util.*;

public class FieldMappingConfigManager {
    /**
     * 获取默认的字段映射配置
     */
    public static List<FieldMappingConfig> getDefaultConfigs() {
        List<FieldMappingConfig> configs = new ArrayList<>();

        // instZone 字段映射配置
        configs.add(new FieldMappingConfig("instZone")
                .headerKeys("instZone", "数据中心", "zone", "inst_zone", "INST_ZONE", "Zone", "数据中心名称")
                .required(true)
                .description("数据中心")
                .validator(ValueValidator.NOT_EMPTY));

        // ip 字段映射配置
        configs.add(new FieldMappingConfig("ip")
                .headerKeys("ip", "IP地址", "IP", "IP_ADDRESS", "address", "主机IP", "服务器IP")
                .required(true)
                .description("IP地址")
                .validator(ValueValidator.IP_VALIDATOR));

        // hostName 字段映射配置
        configs.add(new FieldMappingConfig("hostName")
                .headerKeys("hostName", "主机名", "host", "HOSTNAME", "host_name", "服务器名", "设备名称")
                .description("主机名")
                .validator(ValueValidator.HOSTNAME_VALIDATOR));

        // type 字段映射配置
        configs.add(new FieldMappingConfig("type")
                .headerKeys("type", "类型", "TYPE", "设备类型", "server_type", "machine_type", "设备种类")
                .defaultValue("default")
                .description("设备类型"));

        return configs;
    }

    /**
     * 从Map创建配置（简化方式）
     */
    public static List<FieldMappingConfig> fromMap(Map<String, List<String>> mapping) {
        List<FieldMappingConfig> configs = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
            configs.add(new FieldMappingConfig(entry.getKey(), entry.getValue()));
        }
        return configs;
    }

    /**
     * 从YAML/JSON配置文件加载配置
     */
    public static List<FieldMappingConfig> loadFromConfig(String configPath) {
        // 这里可以根据实际配置文件格式实现
        // 例如从YAML、JSON、Properties等文件加载
        return getDefaultConfigs();
    }

    /**
     * 构建表头映射索引
     * @param headers Excel表头数组
     * @param configs 字段映射配置
     * @return 映射结果，包含字段索引映射和未匹配的表头
     */
    public static MappingResult buildHeaderMapping(String[] headers, List<FieldMappingConfig> configs) {
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        Map<Integer, String> unmatchedHeaders = new HashMap<>();

        // 记录已匹配的索引，防止重复匹配
        Set<Integer> matchedIndexes = new HashSet<>();

        // 先匹配所有配置的字段
        for (FieldMappingConfig config : configs) {
            boolean matched = false;

            for (String key : config.getHeaderKeys()) {
                for (int i = 0; i < headers.length; i++) {
                    // 如果这个索引已经匹配过，跳过
                    if (matchedIndexes.contains(i)) {
                        continue;
                    }

                    String header = headers[i];
                    if (header != null && key != null &&
                            header.trim().equalsIgnoreCase(key.trim())) {
                        // 找到匹配
                        fieldIndexMap.put(config.getTargetField(), i);
                        matchedIndexes.add(i);
                        matched = true;
                        break;
                    }
                }

                if (matched) {
                    break;
                }
            }

            // 必填字段检查
            if (config.isRequired() && !matched) {
                String requiredKeys = String.join(", ", config.getHeaderKeys());
                throw new IllegalArgumentException(
                        String.format("必填字段[%s]未找到对应的表头，可用的表头名称: %s",
                                config.getTargetField(), requiredKeys));
            }
        }

        // 收集未匹配的表头
        for (int i = 0; i < headers.length; i++) {
            if (!matchedIndexes.contains(i) && headers[i] != null) {
                unmatchedHeaders.put(i, headers[i].trim());
            }
        }

        return new MappingResult(fieldIndexMap, unmatchedHeaders);
    }

    /**
     * 映射结果
     */
    @Getter
    public static class MappingResult {
        // 字段名 -> 列索引
        private final Map<String, Integer> fieldIndexMap;
        // 未匹配的列索引 -> 表头名称
        private final Map<Integer, String> unmatchedHeaders;

        public MappingResult(Map<String, Integer> fieldIndexMap, Map<Integer, String> unmatchedHeaders) {
            this.fieldIndexMap = fieldIndexMap;
            this.unmatchedHeaders = unmatchedHeaders;
        }

    }
}
