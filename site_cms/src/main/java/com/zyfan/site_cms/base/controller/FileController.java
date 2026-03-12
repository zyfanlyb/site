package com.zyfan.site_cms.base.controller;

import com.zyfan.site_cms.base.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/file")
@RestController
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/preview")
    public void preview(@RequestParam("fileName")String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileService.preview(fileName, request, response);
    }
}
