package com.zyfan.site_cms.base.controller;

import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.site_cms.base.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/file")
@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问路径（objectName，不包含bucketName）
     */
    @PostMapping("/upload")
    public ResponseVo<String> upload(@RequestParam("file") MultipartFile file) {
        String filePath = fileService.uploadFile(file);
        return ResponseVo.success(filePath);
    }

    /**
     * 预览文件
     * @param objectName 文件路径（objectName）
     * @param response HTTP响应
     */
    @GetMapping("/preview")
    public void preview(
            @RequestParam("objectName") String objectName,
            HttpServletResponse response) {
        fileService.previewFile(objectName, response);
    }

    /**
     * 下载文件
     * @param objectName 文件路径（objectName）
     * @param response HTTP响应
     */
    @GetMapping("/download")
    public void download(
            @RequestParam("objectName") String objectName,
            HttpServletResponse response) {
        fileService.downloadFile(objectName, response);
    }

    /**
     * 删除文件
     * @param objectName 文件路径（objectName）
     * @return 操作结果
     */
    @PostMapping("/delete")
    public ResponseVo<String> delete(@RequestParam("objectName") String objectName) {
        fileService.deleteFile(objectName);
        return ResponseVo.success();
    }

    /**
     * 访问auth项目中的文件，目前只做访问头像
     */
    @PostMapping("/auth/preview")
    public void authPreview(@RequestParam("fileName")String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileService.authPreview(fileName, request, response);
    }
}
