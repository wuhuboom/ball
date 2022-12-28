package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallApp;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallAppMapper;
import com.oxo.ball.service.admin.IBallAppService;
import com.oxo.ball.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@Service
public class BallAppServiceImpl extends ServiceImpl<BallAppMapper, BallApp> implements IBallAppService {
    @Autowired
    RedisUtil redisUtil;
    @Override
    public SearchResponse<BallApp> search(BallApp queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallApp> response = new SearchResponse<>();
        Page<BallApp> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallApp> query = new QueryWrapper<>();
        IPage<BallApp> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallApp findById(Long id) {
        return getById(id);
    }

    @Override
    @Cacheable(value = "ball_app_all",key = "'all'",unless = "#result == null")
    public List<BallApp> findByAll() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        return list(query);
    }

    @Override
    public BallApp insert(BallApp ballApp) {
        ballApp.setStatus(1);
        boolean save = save(ballApp);
        clearCache();
        return ballApp;
    }
    private void clearCache(){
        redisUtil.del("ball_app_all::all");
    }
    @Override
    public Boolean delete(Long id) {
        clearCache();
        return removeById(id);
    }

    @Override
    public Boolean edit(BallApp ballApp) {
        clearCache();
        return updateById(ballApp);
    }

    @Override
    public Boolean status(BallApp notice) {
        BallApp edit = BallApp.builder()
                .status(notice.getStatus())
                .build();
        edit.setId(notice.getId());
        clearCache();
        return updateById(edit);
    }

}
