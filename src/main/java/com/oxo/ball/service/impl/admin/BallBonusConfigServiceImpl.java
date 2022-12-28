package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallBonusConfig;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallBonusConfigMapper;
import com.oxo.ball.service.admin.IBallBonusConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 反佣策略 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallBonusConfigServiceImpl extends ServiceImpl<BallBonusConfigMapper, BallBonusConfig> implements IBallBonusConfigService {
    @Override
    public SearchResponse<BallBonusConfig> search(BallBonusConfig queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBonusConfig> response = new SearchResponse<>();
        Page<BallBonusConfig> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBonusConfig> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(!StringUtils.isBlank(queryParam.getName())){
            query.eq("name",queryParam.getName());
        }
        IPage<BallBonusConfig> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    @CacheEvict(value = "ball_bonus_config",key = "#bonusConfig.getType()")
    public BallBonusConfig insert(BallBonusConfig bonusConfig) {
        bonusConfig.setCreatedAt(System.currentTimeMillis());
        boolean save = save(bonusConfig);
        return bonusConfig;
    }

    @Override
    public BallBonusConfig findById(Long id) {
        return getById(id);
    }


    @Override
    @CacheEvict(value = "ball_bonus_config",key = "#bonusConfig.getType()")
    public Boolean delete(BallBonusConfig bonusConfig) {
        return removeById(bonusConfig.getId());
    }

    @Override
    @CacheEvict(value = "ball_bonus_config",key = "#bonusConfig.getType()")
    public Boolean edit(BallBonusConfig bonusConfig) {
        bonusConfig.setUpdatedAt(System.currentTimeMillis());
        return updateById(bonusConfig);
    }

    @Override
    public Boolean status(BallBonusConfig slideshow) {
        BallBonusConfig edit = BallBonusConfig.builder()
                .status(slideshow.getStatus())
                .build();
        edit.setId(slideshow.getId());
        return updateById(edit);
    }

    @Override
    @Cacheable(value = "ball_bonus_config" , key = "#type",unless = "#result==null")
    public List<BallBonusConfig> findByType(int type) {
        QueryWrapper query = new QueryWrapper();
        query.eq("type",type);
        query.eq("status",1);
        return list(query);
    }
}
