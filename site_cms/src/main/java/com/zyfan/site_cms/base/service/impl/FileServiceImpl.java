package com.zyfan.site_cms.base.service.impl;

import com.zyfan.site_cms.base.service.FileService;
import com.zyfan.client.AuthClient;
import com.zyfan.task.FlushAccessTokenTask;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private AuthClient authClient;
    @Autowired
    private FlushAccessTokenTask flushAccessTokenTask;

    @Override
    public void preview(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
