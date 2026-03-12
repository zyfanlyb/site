package com.zyfan.site_service.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyfan.site_service.cms.entity.CmsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CmsArticleMapper extends BaseMapper<CmsArticle> {

    /**
     * 查询文章列表（带分类名称）
     */
    @Select("SELECT a.*, c.name as category_name " +
            "FROM cms_article a " +
            "LEFT JOIN cms_category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 " +
            "ORDER BY a.sort ASC, a.create_time DESC")
    List<CmsArticle> selectArticleListWithCategory();

    /**
     * 根据分类ID查询文章列表
     */
    @Select("SELECT a.*, c.name as category_name " +
            "FROM cms_article a " +
            "LEFT JOIN cms_category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND a.category_id = #{categoryId} " +
            "ORDER BY a.sort ASC, a.create_time DESC")
    List<CmsArticle> selectArticleListByCategoryId(@Param("categoryId") Long categoryId);

}
