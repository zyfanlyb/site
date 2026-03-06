package com.zyfan.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyfan.cms.entity.CmsSiteSetting;

/**
 * CMS 站点设置服务接口
 * 提供站点配置的获取和更新功能
 * 
 * @author system
 */
public interface ICmsSiteSettingService extends IService<CmsSiteSetting> {
    /**
     * 获取站点设置，如果不存在则自动创建并返回默认值
     * 
     * @return 站点设置对象，包含全站配置的 JSON 字符串
     */
    CmsSiteSetting getOrInit();
    
    /**
     * 更新站点设置的 JSON 配置
     * 
     * @param settingsJson 新的站点配置 JSON 字符串
     */
    void updateSettingsJson(String settingsJson);
}

