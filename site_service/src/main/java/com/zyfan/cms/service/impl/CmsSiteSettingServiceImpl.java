package com.zyfan.cms.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyfan.cms.entity.CmsSiteSetting;
import com.zyfan.cms.mapper.CmsSiteSettingMapper;
import com.zyfan.cms.service.ICmsSiteSettingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * CMS 站点设置服务实现类
 * 
 * @author system
 */
@Service
public class CmsSiteSettingServiceImpl extends ServiceImpl<CmsSiteSettingMapper, CmsSiteSetting> implements ICmsSiteSettingService {

    /**
     * {@inheritDoc}
     * 实现逻辑：查询最新的一条记录，如果不存在则创建一条默认记录（settingsJson 为空对象 JSON）
     */
    @Override
    public CmsSiteSetting getOrInit() {
        CmsSiteSetting setting = this.getOne(Wrappers.lambdaQuery(CmsSiteSetting.class).orderByDesc(CmsSiteSetting::getId).last("limit 1"));
        if (setting != null) {
            return setting;
        }
        CmsSiteSetting init = new CmsSiteSetting();
        init.setSettingsJson(JSON.toJSONString(new LinkedHashMap<>()));
        this.save(init);
        return init;
    }

    /**
     * {@inheritDoc}
     * 实现逻辑：先获取或创建站点设置记录，然后更新 settingsJson 字段
     */
    @Override
    public void updateSettingsJson(String settingsJson) {
        if (StringUtils.isBlank(settingsJson)) {
            settingsJson = "{}";
        }
        CmsSiteSetting setting = this.getOrInit();
        setting.setSettingsJson(settingsJson);
        this.updateById(setting);
    }
}

