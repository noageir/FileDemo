package com.file.demo.util;

import cn.hutool.json.JSONUtil;
import com.file.demo.config.FieldMappingConfig;
import com.file.demo.dto.FileInfo;
import com.file.demo.dto.ParamInfo;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FileAnalysisUtil {
    /**
     * 将FileInfo列表转换为Map，便于查找
     */
    public static Map<String, FileInfo> toMap(List<FileInfo> fileInfos, String keyField) {
        Map<String, FileInfo> map = new LinkedHashMap<>();

        for (FileInfo info : fileInfos) {
            String key = "";
            switch (keyField.toLowerCase()) {
                case "ip":
                    key = info.getIp();
                    break;
                case "hostname":
                    key = info.getHostName();
                    break;
                case "instzone":
                    key = info.getInstZone();
                    break;
                case "type":
                    key = info.getType();
                    break;
                default:
                    key = info.getIp() + "_" + info.getHostName();
            }

            if (!key.isEmpty()) {
                map.put(key, info);
            }
        }

        return map;
    }

    /**
     * 基础分组 - 按单个属性分组
     * 返回默认的 HashMap
     */
    public static <T, K> Map<K, List<T>> groupBy(
            Collection<T> collection,
            Function<? super T, ? extends K> classifier) {
        return groupBy(collection, classifier, HashMap::new, Collectors.toList());
    }

    /**
     * 分组并对分组结果进行转换
     * 完全类型安全，无警告
     */
    public static <T, K, D, A, M extends Map<K, D>> M groupBy(
            Collection<T> collection,
            Function<? super T, ? extends K> classifier,
            Supplier<M> mapFactory,
            Collector<? super T, A, D> downstream) {

        return collection.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        mapFactory,  // 关键：使用传入的工厂创建Map
                        downstream
                ));
    }

    /**
     * 使用HashMap的分组
     */
    public static <T, K, D, A> Map<K, D> groupBy(
            Collection<T> collection,
            Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
        return groupBy(collection, classifier, HashMap::new, downstream);
    }

    /**
     * 保持顺序的分组（LinkedHashMap）
     */
    public static <T, K, D, A> LinkedHashMap<K, D> groupByLinked(
            Collection<T> collection,
            Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
        return groupBy(collection, classifier, LinkedHashMap::new, downstream);
    }

    /**
     * 按键排序的分组（TreeMap）
     */
    public static <T, K extends Comparable<K>, D, A> TreeMap<K, D> groupBySorted(
            Collection<T> collection,
            Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
        return groupBy(collection, classifier, TreeMap::new, downstream);
    }
    /**
     * 获取所有动态参数的Key
     */
    public static Set<String> getAllTitle(List<FileInfo> fileInfos) {
        return fileInfos.stream()
                .flatMap(info -> info.getParamInfo().stream())
                .map(ParamInfo::getKey)
                .collect(Collectors.toSet());
    }


    /**
     * 获取所有动态参数的Key
     */
    public static Set<String> getAllTitle(List<FileInfo> fileInfos, List<FieldMappingConfig> fieldMappingConfigs) {
        Set<String> titleInfo = new LinkedHashSet<>();

        //处理固定的Title
        fieldMappingConfigs.stream()
                .filter(config -> config != null && config.getTargetField() != null)
                .map(FieldMappingConfig::getTitleName)
                .forEach(titleInfo::add);

        //处理动态Title
        fileInfos.stream()
                .filter(info -> info != null && info.getParamInfo() != null)
                .flatMap(info -> info.getParamInfo().stream())
                .filter(param -> param != null && param.getKey() != null)
                .map(ParamInfo::getKey)
                .forEach(titleInfo::add);

        return titleInfo;
    }

    /**
     * 导出为JSON
     */
    public static String toJson(List<FileInfo> fileInfos) {
        return JSONUtil.toJsonPrettyStr(fileInfos);
    }


    /**
     * 统计参数值长度
     */
    public static int getMaxParamInfoSize(List<FileInfo> fileInfoList) {
        if (fileInfoList == null || fileInfoList.isEmpty()) {
            return 0;
        }

        int maxSize = 0;
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo != null && fileInfo.paramInfo != null) {
                int size = fileInfo.paramInfo.size();
                if (size > maxSize) {
                    maxSize = size;
                }
            }
        }
        return maxSize;
    }

    /**
     * 统计信息
     */
    public static Map<String, Object> getStatistics(List<FileInfo> fileInfos) {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("总记录数", fileInfos.size());
        stats.put("IP数量", fileInfos.stream().map(FileInfo::getIp).distinct().count());
        stats.put("主机名数量", fileInfos.stream().map(FileInfo::getHostName).distinct().count());
        stats.put("类型数量", fileInfos.stream().map(FileInfo::getType).distinct().count());

        Map<String, Long> typeCount = fileInfos.stream()
                .collect(Collectors.groupingBy(FileInfo::getType, Collectors.counting()));
        stats.put("类型分布", typeCount);

        Set<String> allParams = getAllTitle(fileInfos);
        stats.put("动态参数总数", allParams.size());
        stats.put("动态参数列表", allParams);

        return stats;
    }
}
