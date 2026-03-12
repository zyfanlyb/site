package com.zyfan.site_cms.base.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface FileService {
    void preview(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
