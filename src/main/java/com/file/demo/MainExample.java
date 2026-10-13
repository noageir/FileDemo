package com.file.demo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.file.demo.config.FieldMappingConfig;
import com.file.demo.dto.FileInfo;
import com.file.demo.service.ValueValidator;
import com.file.demo.util.FileAnalysisUtil;
import com.file.demo.util.FileReaderUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
public class MainExample {

    public final static int FILE_INDEX_NUMBER = 4;


    public static void main(String[] args) {
        Instant start = Instant.now();

        String dataPath = "/Users/xuyi/WorkSpace/Local/FileDemo/data";

        // 示例：读取目录下所有CSV和Excel文件
        List<FileInfo> data1 = FileReaderUtil.readFilesInDir(dataPath, ".*\\.(csv|xlsx|xls)$", FILE_INDEX_NUMBER);

//        List<String> filePaths = Arrays.asList(dataPath + "/test.csv", dataPath + "/test.xlsx");; //dataPath + "/test.csv", dataPath + "/test.xlsx"
//        List<FileInfo> data1 = FileReaderUtil.readFiles(FILE_INDEX_NUMBER, filePaths);

//        List<FileInfo> data1 = FileReaderUtil.readLargeExcelOptimized(dataPath + "/test.xlsx", FILE_INDEX_NUMBER);

        List<FieldMappingConfig> fieldMappingConfigs = initConfig();
//        List<FileInfo> data1 = FileReaderUtil.readLargeExcel(dataPath + "/test.xlsx", fieldMappingConfigs);

        // 示例1：
//        for (FileInfo info : data1) {
//            log.info("固定信息: 数据中心-{} IP地址-{} 主机名-{} 类型-{}", info.getInstZone(), info.getIp(), info.getHostName(), info.getType());
//
//            log.info("动态参数:");
//            log.info("{}{}", StrUtil.padAfter("参数键", 20, ' '), StrUtil.padAfter("参数值", 20, ' '));
//            for (ParamInfo param : info.getParamInfo()) {
//                log.info("{}{}", StrUtil.padAfter(param.getKey(), 23, ' '), StrUtil.padAfter(param.getValue(), 20, ' '));
//            }
//        }

        // 示例2：
//        Map<String, List<FileInfo>> groupedData = FileAnalysisUtil.groupBy(data1, FileInfo::getHostName);
//        for (Map.Entry<String, List<FileInfo>> entry : groupedData.entrySet()) {
//            String key = entry.getKey();
//            List<FileInfo> fileInfoList = entry.getValue();
//            log.info("Map 数据结果： Key-[{}] Value-[{}]", key, fileInfoList);
//        }

        // 示例3：
//        String jsonData = FileAnalysisUtil.toJson(data1);
//        log.info("Json 数据结果：{}", jsonData);

        Map<String, Object> resultInfoMap = FileAnalysisUtil.getStatistics(data1);
        log.info("文件结果信息：{}", resultInfoMap);
        log.info("参数值最大长度：{}", FileAnalysisUtil.getMaxParamInfoSize(data1));
        log.info("参数Key Title:{}", FileAnalysisUtil.getAllTitle(data1, fieldMappingConfigs));

        // 示例4：转换为Map便于查找
//        Map<String, FileInfo> ipMap = FileAnalysisUtil.toMap(data1, "ip");
//        FileInfo target = ipMap.get("127.0.0.1");
//        if (target != null) {
//            log.info("找到IP: {}, 主机名: {}", target.getIp(), target.getHostName());
//        }

        // 示例5：筛选特定条件的数据
//        List<FileInfo> filtered = data1.stream()
//                .filter(info -> "WEB".equals(info.getType()))
//                .filter(info -> info.getParamInfo().stream()
//                        .anyMatch(param -> "status".equals(param.getKey()) && "active".equals(param.getValue())))
//                .collect(Collectors.toList());

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        log.info("执行耗时: {} 秒 {} 毫秒", duration.getSeconds(),(duration.toMillis() % 1000));
    }

    private static List<FieldMappingConfig> initConfig(){
        return Arrays.asList(
                new FieldMappingConfig("instZone")
                        .headerKeys("数据中心", "zone", "区域", "INSTZONE", "inst_zone")
                        .titleName("数据中心")
                        .priority(0)
                        .required(true)
                        .validator(ValueValidator.NOT_EMPTY),

                new FieldMappingConfig("ip")
                        .headerKeys("IP", "IP地址", "主机IP", "server_ip", "ip")
                        .titleName("IP地址")
                        .priority(1)
                        .validator(ValueValidator.IP_VALIDATOR),

                new FieldMappingConfig("hostName")
                        .headerKeys("主机名", "host", "HOSTNAME", "服务器名称", "host_name")
                        .titleName("主机名")
                        .priority(2)
                        .validator(ValueValidator.HOSTNAME_VALIDATOR),

                new FieldMappingConfig("type")
                        .headerKeys("类型", "TYPE", "设备类型", "server_type", "type")
                        .titleName("类型")
                        .priority(3)
                        .defaultValue("unknown")
        );
    }
    


}
