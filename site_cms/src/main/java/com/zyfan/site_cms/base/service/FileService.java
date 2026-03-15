package com.zyfan.site_cms.base.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问路径（objectName，不包含bucketName）
     */
    String uploadFile(MultipartFile file);

    /**
     * 预览文件
     * @param objectName 对象名称（文件路径）
     * @param response HTTP响应
     */
    void previewFile(String objectName, HttpServletResponse response);

    /**
     * 下载文件
     * @param objectName 对象名称（文件路径）
     * @param response HTTP响应
     */
    void downloadFile(String objectName, HttpServletResponse response);

    /**
     * 删除文件
     * @param objectName 对象名称（文件路径）
     */
    void deleteFile(String objectName);


    void authPreview(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
