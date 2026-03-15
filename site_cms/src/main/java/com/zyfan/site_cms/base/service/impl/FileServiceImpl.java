package com.zyfan.site_cms.base.service.impl;

import com.zyfan.exception.ZException;
import com.zyfan.pojo.web.CodeStatusEnum;
import com.zyfan.site_cms.base.service.FileService;
import com.zyfan.client.AuthClient;
import com.zyfan.task.FlushAccessTokenTask;
import io.minio.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private AuthClient authClient;
    @Autowired
    private FlushAccessTokenTask flushAccessTokenTask;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ZException(CodeStatusEnum.FAILED, "文件不能为空");
        }

        try {
            // 确保bucket存在
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                throw new ZException(CodeStatusEnum.FAILED, "文件上传失败: bucket不存在");
            }

            // 生成文件路径：按日期分类存储
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String objectName = datePath + "/" + fileName;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回文件路径（只返回objectName，不包含bucketName）
            return objectName;
        } catch (Exception e) {
            throw new ZException(CodeStatusEnum.FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void previewFile(String objectName, HttpServletResponse response) {
        if (StringUtils.isBlank(objectName)) {
            throw new ZException(CodeStatusEnum.FAILED, "文件路径不能为空");
        }

        try {
            // 获取文件对象
            GetObjectResponse objectResponse = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 获取文件信息
            StatObjectResponse statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 设置响应头
            String contentType = statObject.contentType();
            if (StringUtils.isBlank(contentType)) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"" + getFileName(objectName) + "\"");

            // 写入响应流
            try (InputStream inputStream = objectResponse;
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new ZException(CodeStatusEnum.FAILED, "文件预览失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(String objectName, HttpServletResponse response) {
        if (StringUtils.isBlank(objectName)) {
            throw new ZException(CodeStatusEnum.FAILED, "文件路径不能为空");
        }

        try {
            // 获取文件对象
            GetObjectResponse objectResponse = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 获取文件信息
            StatObjectResponse statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            // 设置响应头（下载）
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + getFileName(objectName) + "\"");
            response.setHeader("Content-Length", String.valueOf(statObject.size()));

            // 写入响应流
            try (InputStream inputStream = objectResponse;
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new ZException(CodeStatusEnum.FAILED, "文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String objectName) {
        if (StringUtils.isBlank(objectName)) {
            throw new ZException(CodeStatusEnum.FAILED, "文件路径不能为空");
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new ZException(CodeStatusEnum.FAILED, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 从objectName中提取文件名
     */
    private String getFileName(String objectName) {
        if (objectName.contains("/")) {
            return objectName.substring(objectName.lastIndexOf("/") + 1);
        }
        return objectName;
    }

    @Override
    public void authPreview(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(fileName)) {
            return;
        }
        ResponseEntity<Resource> responseEntity = null;
        try {
            responseEntity = authClient.filePreview(fileName,request.getHeader("Authorization"),flushAccessTokenTask.getAccessToken());
            if (responseEntity==null||responseEntity.getBody()==null||responseEntity.getBody().getInputStream()==null||responseEntity.getBody().getInputStream().available()==0) {
                throw new Exception();
            }
        } catch (Exception e) {
            flushAccessTokenTask.accessToken = null;
            flushAccessTokenTask.flushAccessToken();
            responseEntity = authClient.filePreview(fileName,request.getHeader("Authorization"),flushAccessTokenTask.getAccessToken());
        }
        if (responseEntity==null||responseEntity.getBody()==null||responseEntity.getBody().getInputStream()==null||responseEntity.getBody().getInputStream().available()==0) {
            return;
        }
        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        InputStream inputStream = responseEntity.getBody().getInputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, length);
        }
        response.getOutputStream().close();
    }
}
