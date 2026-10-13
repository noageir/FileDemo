package com.file.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileInfo {
    /**
     * 数据中心
     */
    public String instZone;

    /**
     * IP地址
     */
    public String ip;

    /**
     * 主机名
     */
    public String hostName;

    /**
     * 类型
     */
    public String type;

    /**
     * 参数集
     */
    public List<ParamInfo> paramInfo;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 扩展名
     */
    private String extensionName;

}
