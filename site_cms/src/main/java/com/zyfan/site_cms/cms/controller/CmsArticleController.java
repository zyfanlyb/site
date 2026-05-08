package com.zyfan.site_cms.cms.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zyfan.site_cms.anno.NoAuth;
import com.zyfan.site_cms.anno.Permission;
import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.zyfan.site_cms.cms.service.ICmsArticleService;
import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CMS文章管理
 */
@RestController
@RequestMapping("/cms/article")
public class CmsArticleController {

    @Autowired
    private ICmsArticleService cmsArticleService;

    @NoAuth
    @Permission("cms:article:page")
    @PostMapping("/page")
    public ResponseVo<List<CmsArticle>> page(@RequestBody RequestVo<CmsArticle> requestVo) {
        return ResponseVo.success(cmsArticleService.pageList(requestVo));
    }

    @Permission("cms:article:add")
    @PostMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseVo insert(@RequestBody RequestVo<CmsArticle> requestVo) {
        cmsArticleService.insert(requestVo);
        return ResponseVo.success();
    }

    /**
     * 新增文章（封面与 JSON 一次提交）：{@code data} 为 {@link RequestVo} 的 JSON；
     * {@code coverImages} 中保留的已有 key 写原串，新图位置用空串占位，新文件按顺序传 {@code coverFiles}。
     */
    @Permission("cms:article:add")
    @PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVo insertMultipart(@RequestPart("data") String dataJson,
            @RequestPart(value = "coverFiles", required = false) MultipartFile[] coverFiles,
            @RequestPart(value = "inlineFiles", required = false) MultipartFile[] inlineFiles,
            @RequestPart(value = "inlineIndices", required = false) String inlineIndicesJson) {
        RequestVo<CmsArticle> vo = JSON.parseObject(dataJson, new TypeReference<RequestVo<CmsArticle>>() {});
        List<MultipartFile> covers = coverFiles == null ? List.of()
                : Arrays.stream(coverFiles).filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
        List<MultipartFile> inlines = inlineFiles == null ? List.of()
                : Arrays.stream(inlineFiles).filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
        List<Integer> inlineIdx = parseInlineIndices(inlineIndicesJson);
        cmsArticleService.insert(vo, covers, inlines, inlineIdx);
        return ResponseVo.success();
    }

    @Permission("cms:article:edit")
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseVo update(@RequestBody RequestVo<CmsArticle> requestVo) {
        cmsArticleService.update(requestVo);
        return ResponseVo.success();
    }

    /** 更新文章，规则同 {@link #insertMultipart}。 */
    @Permission("cms:article:edit")
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVo updateMultipart(@RequestPart("data") String dataJson,
            @RequestPart(value = "coverFiles", required = false) MultipartFile[] coverFiles,
            @RequestPart(value = "inlineFiles", required = false) MultipartFile[] inlineFiles,
            @RequestPart(value = "inlineIndices", required = false) String inlineIndicesJson) {
        RequestVo<CmsArticle> vo = JSON.parseObject(dataJson, new TypeReference<RequestVo<CmsArticle>>() {});
        List<MultipartFile> covers = coverFiles == null ? List.of()
                : Arrays.stream(coverFiles).filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
        List<MultipartFile> inlines = inlineFiles == null ? List.of()
                : Arrays.stream(inlineFiles).filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());
        List<Integer> inlineIdx = parseInlineIndices(inlineIndicesJson);
        cmsArticleService.update(vo, covers, inlines, inlineIdx);
        return ResponseVo.success();
    }

    private static List<Integer> parseInlineIndices(String inlineIndicesJson) {
        if (inlineIndicesJson == null || inlineIndicesJson.isBlank()) {
            return List.of();
        }
        List<Integer> list = JSON.parseArray(inlineIndicesJson.trim(), Integer.class);
        return list == null ? List.of() : list;
    }

    @NoAuth
    @Permission("cms:article:view")
    @PostMapping("/info/{id}")
    public ResponseVo<CmsArticle> info(@PathVariable("id") Long id) {
        return ResponseVo.success(cmsArticleService.info(id));
    }

    @Permission("cms:article:delete")
    @PostMapping("/remove/{id}")
    public ResponseVo remove(@PathVariable("id") Long id) {
        cmsArticleService.remove(id);
        return ResponseVo.success();
    }

}
