package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallIpCountry;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallIpCountryMapper;
import com.oxo.ball.service.admin.IBallIpCountryService;
import com.oxo.ball.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 系统公告 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallIpCountryServiceImpl extends ServiceImpl<BallIpCountryMapper, BallIpCountry> implements IBallIpCountryService {
    @Autowired
    RedisUtil redisUtil;

    @Override
    public SearchResponse<BallIpCountry> search(BallIpCountry queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallIpCountry> response = new SearchResponse<>();
        Page<BallIpCountry> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallIpCountry> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        IPage<BallIpCountry> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallIpCountry insert(BallIpCountry slideshow) {
        slideshow.setStatus(1);
        slideshow.setCreatedAt(System.currentTimeMillis());
        try {
            save(slideshow);
        }catch (Exception ex){}
        return slideshow;
    }

    @Override
    @Cacheable(value = "ball_ip_country", key = "#id", unless = "#result == null")
    public BallIpCountry findById(Long id) {
        return getById(id);
    }

    @Override
    public Boolean delete(Long id) {
        clearCache(id);
        return removeById(id);
    }

    @Override
    public Boolean edit(BallIpCountry ipCountry) {
        clearCache(ipCountry.getId());
        return updateById(ipCountry);
    }

    @Override
    public Boolean status(BallIpCountry ipCountry) {
        BallIpCountry edit = BallIpCountry.builder()
                .status(ipCountry.getStatus())
                .build();
        edit.setId(ipCountry.getId());
        clearCache(ipCountry.getId());
        return updateById(edit);
    }
    private void clearCache(Long id){
        BallIpCountry byId = findById(id);
        redisUtil.del("ball_ip_country::"+byId.getCountry());
        redisUtil.del("ball_ip_country::"+id);
    }
    @Override
    public List<BallIpCountry> findAll() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        return list(query);
    }

    @Override
    @Cacheable(value = "ball_ip_country", key = "#country", unless = "#result == null")
    public BallIpCountry findByCountry(String country) {
        QueryWrapper query = new QueryWrapper();
        query.eq("country",country);
        return getOne(query);
    }
}
