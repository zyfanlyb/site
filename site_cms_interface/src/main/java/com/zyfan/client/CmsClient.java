package com.zyfan.client;

import com.zyfan.pojo.web.RequestVo;
import com.zyfan.pojo.web.ResponseVo;
import com.zyfan.vo.CmsArticleVo;
import com.zyfan.vo.CmsCategoryVo;
import com.zyfan.vo.CmsTypeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "site-cms")
public interface CmsClient {

    /**
     * 文章分页查询：cms 返回 IPage JSON，这里用 List<CmsArticleVo> 承接
     */
    @PostMapping("/siteCms/cms/article/page")
    ResponseVo<List<CmsArticleVo>> pageArticles(@RequestBody RequestVo<CmsArticleVo> requestVo);

    /**
     * 文章详情：cms 返回 CmsArticle，这里用 CmsArticleVo 承接
     */
    @PostMapping("/siteCms/cms/article/info/{id}")
    ResponseVo<CmsArticleVo> getArticleInfo(@PathVariable("id") Long id);

    /**
     * 分类列表（启用的）
     */
    @PostMapping("/siteCms/cms/category/list")
    ResponseVo<List<CmsCategoryVo>> listCategories();

    /**
     * 类型列表（按分类ID过滤）
     */
    @PostMapping("/siteCms/cms/type/list")
    ResponseVo<List<CmsTypeVo>> listTypes(@RequestBody RequestVo<CmsTypeVo> requestVo);
}
