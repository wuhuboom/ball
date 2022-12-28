package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallCommissionStrategy;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallCommissionStrategyMapper;
import com.oxo.ball.service.admin.IBallCommissionStrategyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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
public class BallCommissionStrategyServiceImpl extends ServiceImpl<BallCommissionStrategyMapper, BallCommissionStrategy> implements IBallCommissionStrategyService {
    @Override
    public SearchResponse<BallCommissionStrategy> search(BallCommissionStrategy queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallCommissionStrategy> response = new SearchResponse<>();
        Page<BallCommissionStrategy> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallCommissionStrategy> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getName())){
            query.eq("name",queryParam.getName());
        }
        if(queryParam.getCommissionStrategyType()!=null){
            query.eq("commission_strategy_type",queryParam.getCommissionStrategyType());
        }
        if(queryParam.getAutomaticDistribution()!=null){
            query.eq("automatic_distribution",queryParam.getAutomaticDistribution());
        }

        IPage<BallCommissionStrategy> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    @CacheEvict(value = "ball_commission_strategy",key = "#commissionStrategy.getCommissionStrategyType()")
    public BallCommissionStrategy insert(BallCommissionStrategy commissionStrategy) {
        commissionStrategy.setCreatedAt(System.currentTimeMillis());
        boolean save = save(commissionStrategy);
        return commissionStrategy;
    }

    @Override
    public BallCommissionStrategy findById(Long id) {
        return getById(id);
    }


    @Override
    @CacheEvict(value = "ball_commission_strategy",key = "#commissionStrategy.getCommissionStrategyType()")
    public Boolean delete(BallCommissionStrategy commissionStrategy) {
        return removeById(commissionStrategy.getId());
    }

    @Override
    @CacheEvict(value = "ball_commission_strategy",key = "#commissionStrategy.getCommissionStrategyType()")
    public Boolean edit(BallCommissionStrategy commissionStrategy) {
        commissionStrategy.setUpdatedAt(System.currentTimeMillis());
        return updateById(commissionStrategy);
    }

    @Override
    public Boolean status(BallCommissionStrategy slideshow) {
        BallCommissionStrategy edit = BallCommissionStrategy.builder()
                .status(slideshow.getStatus())
                .build();
        edit.setId(slideshow.getId());
        return updateById(edit);
    }

    @Override
    @Cacheable(value = "ball_commission_strategy" , key = "#type",unless = "#result==null")
    public List<BallCommissionStrategy> findByType(int type) {
        QueryWrapper query = new QueryWrapper();
        query.eq("commission_strategy_type",type);
        query.eq("status",1);

        query.orderByAsc("commission_level");
        return list(query);
    }
}
