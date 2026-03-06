package com.zyfan.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * CMS 站点设置实体类
 * 用于存储全站通用配置信息，如站点标题、logo、导航菜单、页脚信息、主题色、社交媒体链接等
 * 
 * @author system
 */
@Data
@TableName("cms_site_setting")
public class CmsSiteSetting {
    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 站点配置JSON字符串
     * 存储格式示例：
     * {
     *   "siteTitle": "我的个人网站",
     *   "siteSubtitle": "欢迎访问",
     *   "logo": "/images/logo.png",
     *   "favicon": "/images/favicon.ico",
     *   "themeColor": "#1890ff",
     *   "navigation": [
     *     {"label": "首页", "path": "/"},
     *     {"label": "博客", "path": "/blog"},
     *     {"label": "项目", "path": "/projects"}
     *   ],
     *   "socialLinks": {
     *     "github": "https://github.com/xxx",
     *     "email": "xxx@example.com"
     *   },
     *   "footer": {
     *     "copyright": "© 2024",
     *     "icp": "京ICP备xxx号"
     *   }
     * }
     */
    @TableField("settings_json")
    private String settingsJson;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Date updateTime;
}

