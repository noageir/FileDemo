package com.file.demo.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.file.demo.config.FieldMappingConfig;
import com.file.demo.config.FieldMappingConfigManager;
import com.file.demo.dto.ExcelContext;
import com.file.demo.dto.FileInfo;
import com.file.demo.dto.ParamInfo;
import com.file.demo.dto.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class FileReaderUtil {

    /**
     * 读取文件
     */
    public static List<FileInfo> read(String filePath, int titleIndex) {
        String extName = FileUtil.extName(filePath).toLowerCase();

        switch (extName) {
            case "csv":
                return readCsv(filePath, titleIndex);
            case "xlsx":
            case "xls":
                return readExcel(filePath, titleIndex);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 批量读取文件
     */
    public static List<FileInfo> readFiles(int titleIndex, List<String> filePaths) {
        List<FileInfo> allResults = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                allResults.addAll(read(filePath, titleIndex));
            } catch (Exception e) {
                log.error("读取文件:[{}] 失败, 失败原因：{}", filePath, e.getMessage(), e);
            }
        }
        return allResults;
    }

    /**
     * 读取目录下所有匹配文件
     */
    public static List<FileInfo> readFilesInDir(String dirPath, String pattern, int titleIndex) {
        List<FileInfo> allResults = new ArrayList<>();
        List<File> files = FileUtil.loopFiles(dirPath, file -> file.getName().matches(pattern.replace("*", ".*")));

        for (File file : files) {
            try {
                String fileUrl= file.getAbsolutePath();
                allResults.addAll(read(fileUrl, titleIndex));
            } catch (Exception e) {
                log.error("读取文件:[{}] 失败, 失败原因：{}", file.getAbsolutePath(), e.getMessage(), e);
            }
        }
        return allResults;
    }


    /**
     * 读取文件-CSV
     */
    private static List<FileInfo> readCsv(String filePath, int titleIndex) {
        List<FileInfo> result = new ArrayList<>();

        CsvReader reader = CsvUtil.getReader();
        File file = FileUtil.file(filePath);
        CsvData data = reader.read(file, CharsetUtil.CHARSET_UTF_8);
        String fileName = file.getName();
        String extensionName = StrUtil.subAfter(fileName, ".", true);

        List<CsvRow> rows = data.getRows();
        if (rows.isEmpty()) {
            return result;
        }

        // 第一行是标题
        CsvRow headerRow = rows.get(0);
        List<String> headers = headerRow.getRawList();

        if (headers.size() < titleIndex) {
            log.info("csv 文件长度不符合要求,文档长度为：{}", headers.size());
            //后续处理文件处理结果统计
        }

        // 从第二行开始读取数据
        for (int i = 1; i < rows.size(); i++) {
            CsvRow row = rows.get(i);
            List<String> fields = row.getRawList();

            FileInfo fileInfo = new FileInfo();
            //固定文件头前部内容
            fileInfo.setInstZone(!fields.isEmpty() ? fields.get(0) : "");
            fileInfo.setIp(fields.size() > 1 ? fields.get(1) : "");
            fileInfo.setHostName(fields.size() > 2 ? fields.get(2) : "");
            fileInfo.setType(fields.size() > 3 ? fields.get(3) : "");


            fileInfo.setFileName(fileName);
            fileInfo.setExtensionName(extensionName);

            List<ParamInfo> paramInfos = new ArrayList<>();
            // 动态列处理
            for (int j = titleIndex; j < headers.size(); j++) {
                String paramKey = headers.get(j);
                String value = fields.get(j);
                ParamInfo paramInfo = new ParamInfo();
                paramInfo.setKey(paramKey);
                paramInfo.setValue(value);
                paramInfos.add(paramInfo);
            }

            fileInfo.setParamInfo(paramInfos);
            result.add(fileInfo);
        }

        return result;
    }

    /**
     * 读取文件-Excel
     */
    private static List<FileInfo> readExcel(String filePath, int titleIndex) {
        List<FileInfo> result = new ArrayList<>();

        File file = FileUtil.file(filePath);
        ExcelReader reader = ExcelUtil.getReader(file);
        String fileName = file.getName();
        String extensionName = StrUtil.subAfter(fileName, ".", true);

        // 获取所有工作表
        int sheetCount = reader.getSheetCount();
        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            reader.setSheet(sheetIndex);

            // 读取标题行
            List<Object> headerList = reader.readRow(0);
            if (headerList.size() < titleIndex) {
                log.info("Excel文件长度不符合要求,文档长度为：{}", headerList.size());
                //后续处理文件处理结果统计
                return new ArrayList<>();
            }

            // 读取所有行
            int rowCount = reader.getRowCount();
            for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
                List<Object> row = reader.readRow(rowIndex);
                if (row == null || row.size() < titleIndex) {
                    continue;
                }

                FileInfo fileInfo = new FileInfo();
                //固定文件头前部内容
                fileInfo.setInstZone(!row.isEmpty() ? String.valueOf(row.get(0)) : "");
                fileInfo.setIp(row.size() > 1 ? String.valueOf(row.get(1)) : "");
                fileInfo.setHostName(row.size() > 2 ? String.valueOf(row.get(2)) : "");
                fileInfo.setType(row.size() > 3 ? String.valueOf(row.get(3)) : "");

                fileInfo.setFileName(fileName);
                fileInfo.setExtensionName(extensionName);

                // 动态列处理
                List<ParamInfo> paramInfos = new ArrayList<>();
                for (int x = titleIndex; x < headerList.size(); x++) {
                    String paramKey = String.valueOf(headerList.get(x));
                    String value = String.valueOf(row.get(x));
                    ParamInfo paramInfo = new ParamInfo();
                    paramInfo.setKey(paramKey);
                    paramInfo.setValue(value);
                    paramInfos.add(paramInfo);

                }
                fileInfo.setParamInfo(paramInfos);
                result.add(fileInfo);
            }
        }

        reader.close();
        return result;
    }

    /**
     * 读取大型Excel文件（节省内存）
     */
    public static List<FileInfo> readLargeExcelOptimized(String filePath, int titleIndex) {
        // 参数校验
        List<FileInfo> result = new ArrayList<>();
        final String[][] headers = {null};

        // 文件信息只需提取一次
        String fileName = FilenameUtils.getName(filePath);
        String extensionName = FilenameUtils.getExtension(fileName);

        ExcelUtil.readBySax(filePath, -1, (sheetIndex, rowIndex, rowList) -> {
            if (rowIndex == 0) {
                // 初始化标题数组
                headers[0] = new String[rowList.size()];
                for (int i = 0; i < rowList.size(); i++) {
                    headers[0][i] = String.valueOf(rowList.get(i));
                }
            } else if (headers[0] != null) {
                FileInfo fileInfo = createFileInfo(rowList, headers[0], titleIndex, fileName, extensionName);
                if (fileInfo != null) {
                    result.add(fileInfo);
                }
            }
        });

        return result;
    }

    private static FileInfo createFileInfo(List<Object> rowList, String[] headers, int titleIndex, String fileName, String extensionName) {
        if (CollUtil.isEmpty(rowList)) {
            return null;
        }

        FileInfo fileInfo = new FileInfo();

        // 使用数组访问提高性能
        Object[] rowArray = rowList.toArray();

        // 设置固定字段
        if (rowArray.length > 0) fileInfo.setInstZone(Convert.toStr(rowArray[0], ""));
        if (rowArray.length > 1) fileInfo.setIp(Convert.toStr(rowArray[1], ""));
        if (rowArray.length > 2) fileInfo.setHostName(Convert.toStr(rowArray[2], ""));
        if (rowArray.length > 3) fileInfo.setType(Convert.toStr(rowArray[3], ""));

        // 设置文件信息
        fileInfo.setFileName(fileName);
        fileInfo.setExtensionName(extensionName);

        // 处理动态列
        if (headers.length > titleIndex) {
            List<ParamInfo> paramInfos = new ArrayList<>(headers.length - titleIndex);

            for (int i = titleIndex; i < headers.length; i++) {
                if (i < rowArray.length) {
                    ParamInfo paramInfo = new ParamInfo();
                    paramInfo.setKey(headers[i]);
                    paramInfo.setValue(Convert.toStr(rowArray[i], ""));
                    paramInfos.add(paramInfo);
                }
            }

            fileInfo.setParamInfo(paramInfos);
        }

        return fileInfo;
    }

    /**
     * 读取大型Excel文件（配置化版本）
     */
    public static List<FileInfo> readLargeExcel(String filePath, List<FieldMappingConfig> fieldConfigs) {
        // 参数校验
        Objects.requireNonNull(filePath, "文件路径不能为空");
        Objects.requireNonNull(fieldConfigs, "字段配置不能为空");

        if (!FileUtil.exist(filePath)) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        List<FileInfo> result = new ArrayList<>();
        String fileName = FileUtil.getName(filePath);
        String extensionName = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf(".") + 1) : "";

        // 读取Excel
        ExcelUtil.readBySax(FileUtil.file(filePath), -1, (sheetIndex, rowIndex, rowList) -> {
            if (rowIndex == 0) {
                // 处理表头行
                processHeaders(rowList, fieldConfigs, result, fileName, extensionName);
            } else if (rowIndex > 0 && !rowList.isEmpty()) {
                // 处理数据行
                FileInfo fileInfo = processDataRow(rowList, fileName, extensionName);
                if (fileInfo != null) {
                    synchronized (result) {
                        result.add(fileInfo);
                    }
                }
            }
        });

        return result;
    }

    /**
     * 使用默认配置读取Excel
     */
    public static List<FileInfo> readLargeExcel(String filePath) {
        return readLargeExcel(filePath, FieldMappingConfigManager.getDefaultConfigs());
    }

    /**
     * 处理数据行
     */
    private static FileInfo processDataRow(List<Object> rowList,
                                           String fileName,
                                           String extensionName) {
        if (CollUtil.isEmpty(rowList)) {
            return null;
        }

        ExcelContext context = ExcelContext.getContext();
        if (context == null || context.getHeaders() == null) {
            log.warn("Excel上下文未初始化，跳过数据行处理");
            return null;
        }

        FileInfo fileInfo = new FileInfo();

        // 1. 设置文件信息（不从Excel读取，从文件信息获取）
        fileInfo.setFileName(context.getFileName());
        fileInfo.setExtensionName(context.getExtensionName());

        // 2. 处理映射的字段
        Map<String, Integer> fieldIndexMap = context.getFieldIndexMap();
        List<FieldMappingConfig> fieldConfigs = context.getFieldConfigs();

        for (FieldMappingConfig config : fieldConfigs) {
            Integer columnIndex = fieldIndexMap.get(config.getTargetField());
            String value = "";

            if (columnIndex != null && columnIndex < rowList.size()) {
                // 从Excel读取值
                Object rawValue = rowList.get(columnIndex);
                value = Convert.toStr(rawValue, "");
            } else {
                // 使用默认值
                value = config.getDefaultValue();
            }

            // 值校验
            if (config.getValidator() != null) {
                String headerKey = columnIndex != null && columnIndex < context.getHeaders().length ?
                        context.getHeaders()[columnIndex] : config.getTargetField();
                ValidationResult validationResult = config.getValidator().validate(value, config.getTargetField(), headerKey);

                if (!validationResult.isValid()) {
                    log.warn("字段[{}]校验失败: {}", config.getTargetField(), validationResult.getMessage());

                    if (config.isRequired() && (value == null || value.trim().isEmpty())) {
                        throw new IllegalArgumentException(
                                String.format("必填字段[%s]为空，校验失败: %s",
                                        config.getTargetField(), validationResult.getMessage()));
                    }
                }

                if (validationResult.hasCorrectedValue()) {
                    value = validationResult.getCorrectedValue();
                }
            }

            // 设置字段值
            fileInfo.setFileName(fileName);
            fileInfo.setExtensionName(extensionName);
            setFieldValue(fileInfo, config.getTargetField(), value);
        }

        // 3. 处理未匹配的字段（放入paramInfo）
        List<ParamInfo> paramInfos = new ArrayList<>();
        Map<Integer, String> unmatchedHeaders = context.getUnmatchedHeaders();

        for (Map.Entry<Integer, String> entry : unmatchedHeaders.entrySet()) {
            Integer columnIndex = entry.getKey();
            String header = entry.getValue();

            if (columnIndex < rowList.size()) {
                String paramValue = Convert.toStr(rowList.get(columnIndex), "");

                ParamInfo paramInfo = new ParamInfo();
                paramInfo.setKey(header);
                paramInfo.setValue(paramValue);
                paramInfos.add(paramInfo);
            }
        }

        fileInfo.setParamInfo(paramInfos);

        return fileInfo;
    }

    /**
     * 设置字段值
     */
    private static void setFieldValue(FileInfo fileInfo, String fieldName, String value) {
        try {
            Field field = FileInfo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(fileInfo, value);
        } catch (NoSuchFieldException e) {
            // 如果是私有字段fileName或extensionName
            if ("fileName".equals(fieldName)) {
                fileInfo.setFileName(value);
            } else if ("extensionName".equals(fieldName)) {
                fileInfo.setExtensionName(value);
            } else {
                log.warn("未找到字段[{}]，跳过设置", fieldName);
            }
        } catch (IllegalAccessException e) {
            log.warn("设置字段[{}]失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 处理表头行
     * 这里通过ThreadLocal在不同行之间传递映射关系
     */
    private static void processHeaders(List<Object> rowList,
                                       List<FieldMappingConfig> fieldConfigs,
                                       List<FileInfo> result,
                                       String fileName,
                                       String extensionName) {
        // 提取表头数组
        String[] headers = rowList.stream()
                .map(obj -> Convert.toStr(obj, ""))
                .toArray(String[]::new);

        // 构建表头映射
        FieldMappingConfigManager.MappingResult mappingResult =
                FieldMappingConfigManager.buildHeaderMapping(headers, fieldConfigs);

        // 将映射结果存入ThreadLocal，供数据处理使用
        ExcelContext context = ExcelContext.getContext();
        context.setHeaders(headers);
        context.setFieldIndexMap(mappingResult.getFieldIndexMap());
        context.setUnmatchedHeaders(mappingResult.getUnmatchedHeaders());
        context.setFieldConfigs(fieldConfigs);
        context.setFileName(fileName);
        context.setExtensionName(extensionName);
    }

}
