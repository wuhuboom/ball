package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallIpWhiteMapper;
import com.oxo.ball.service.admin.IBallIpWhiteService;
import org.apache.commons.lang3.StringUtils;
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
public class BallIpWhiteServiceImpl extends ServiceImpl<BallIpWhiteMapper, BallIpWhite> implements IBallIpWhiteService {
    @Override
    public SearchResponse<BallIpWhite> search(BallIpWhite queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallIpWhite> response = new SearchResponse<>();
        Page<BallIpWhite> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallIpWhite> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        IPage<BallIpWhite> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    @CacheEvict(value = "ball_ip_white_list",key = "'all'")
    public BallIpWhite insert(BallIpWhite slideshow) {
        slideshow.setStatus(1);
        slideshow.setCreatedAt(System.currentTimeMillis());
        boolean save = save(slideshow);
        return slideshow;
    }

    @Override
    @CacheEvict(value = "ball_ip_white_list",key = "'all'")
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    @CacheEvict(value = "ball_ip_white_list",key = "'all'")
    public Boolean edit(BallIpWhite slideshow) {
        return updateById(slideshow);
    }

    @Override
    @CacheEvict(value = "ball_ip_white_list",key = "'all'")
    public Boolean status(BallIpWhite ipWhite) {
        BallIpWhite edit = BallIpWhite.builder()
                .status(ipWhite.getStatus())
                .build();
        edit.setId(ipWhite.getId());

        return updateById(edit);
    }

    @Override
    @Cacheable(value = "ball_ip_white_list", key = "'all'", unless = "#result == null")
    public List<BallIpWhite> findAll() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        return list(query);
    }
}
