package com.zyfan.site_cms.cms.service;

import com.zyfan.site_cms.cms.entity.CmsArticle;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zyfan.pojo.web.RequestVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章服务（ES 主存储版本）。
 *
 * <p>注意：不再继承 MyBatis-Plus 的 IService，避免引入与“废弃文章表”相冲突的数据库通用能力。</p>
 */
public interface ICmsArticleService {

    IPage<CmsArticle> pageList(RequestVo<CmsArticle> requestVo);

    List<CmsArticle> list(RequestVo<CmsArticle> requestVo);

    void insert(RequestVo<CmsArticle> requestVo);

    /**
     * 新增文章：{@code coverImages} 与 {@code coverFiles} 按顺序合并——非空串为已有 objectKey，空串占位表示使用 {@code coverFiles} 中下一张新图。
     */
    void insert(RequestVo<CmsArticle> requestVo, List<MultipartFile> coverFiles);

    void update(RequestVo<CmsArticle> requestVo);

    /** 更新文章，封面合并规则同 {@link #insert(RequestVo, List)}。 */
    void update(RequestVo<CmsArticle> requestVo, List<MultipartFile> coverFiles);

    /**
     * 新增文章（封面 + 正文内联图一次提交）。正文 Markdown 中可含占位 {@code __CMS_INLINE_索引__}，
     * 与 {@code inlineIndices}、{@code inlineFiles} 顺序一一对应，保存时替换为 MinIO objectKey。
     */
    void insert(RequestVo<CmsArticle> requestVo, List<MultipartFile> coverFiles,
            List<MultipartFile> inlineFiles, List<Integer> inlineIndices);

    /** 更新文章，内联图规则同 {@link #insert(RequestVo, List, List, List)}。 */
    void update(RequestVo<CmsArticle> requestVo, List<MultipartFile> coverFiles,
            List<MultipartFile> inlineFiles, List<Integer> inlineIndices);

    CmsArticle info(Long id);

    void remove(Long id);

}
