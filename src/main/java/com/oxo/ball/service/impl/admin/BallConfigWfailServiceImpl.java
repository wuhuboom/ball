package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallConfigWfail;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallConfigWfailMapper;
import com.oxo.ball.service.admin.IBallConfigWfailService;
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
public class BallConfigWfailServiceImpl extends ServiceImpl<BallConfigWfailMapper, BallConfigWfail> implements IBallConfigWfailService {
    @Autowired
    RedisUtil redisUtil;
    @Override
    public SearchResponse<BallConfigWfail> search(BallConfigWfail queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallConfigWfail> response = new SearchResponse<>();
        Page<BallConfigWfail> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallConfigWfail> query = new QueryWrapper<>();
        IPage<BallConfigWfail> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallConfigWfail findById(Long id) {
        return getById(id);
    }

    @Override
    @Cacheable(value = "ball_config_wfail",key = "'all'",unless = "#result == null")
    public List<BallConfigWfail> findByAll() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        return list(query);
    }

    @Override
    public BallConfigWfail insert(BallConfigWfail configWfail) {
        configWfail.setStatus(1);
        boolean save = save(configWfail);
        clearCache();
        return configWfail;
    }
    private void clearCache(){
        redisUtil.del("ball_config_wfail::all");
    }
    @Override
    public Boolean delete(Long id) {
        clearCache();
        return removeById(id);
    }

    @Override
    public Boolean edit(BallConfigWfail configWfail) {
        clearCache();
        return updateById(configWfail);
    }

    @Override
    public Boolean status(BallConfigWfail notice) {
        BallConfigWfail edit = BallConfigWfail.builder()
                .status(notice.getStatus())
                .build();
        edit.setId(notice.getId());
        clearCache();
        return updateById(edit);
    }

}
