package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallCountryMapper;
import com.oxo.ball.service.admin.IBallCountryService;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author flooming
 */
@Service
public class BallCountryServiceImpl extends ServiceImpl<BallCountryMapper, BallCountry> implements IBallCountryService {
    @Resource
    BallCountryMapper ballCountryMapper;
    @Resource
    RedisUtil redisUtil;

    @Override
    public BallCountry findById(Long id) {
        return ballCountryMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "ball_country",key = "'all'")
    public List<BallCountry> findAll() {
        return list();
    }

    @Override
    @CacheEvict(value = "ball_country",key = "'all'")
    public BaseResponse insert(BallCountry ballGroup) {
        try {
            ballCountryMapper.insert(ballGroup);
        }catch (Exception e){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e22"));
        }
        return BaseResponse.SUCCESS;
    }

    @Override
    @CacheEvict(value = "ball_country",key = "'all'")
    public Boolean delete(Long id) {
        boolean suc = (ballCountryMapper.deleteById(id) == 1);
        return suc;
    }

    @Override
    @CacheEvict(value = "ball_country",key = "'all'")
    public BaseResponse edit(BallCountry editBallCountry) {
        //清除原来的授权
        try {
            ballCountryMapper.updateById(editBallCountry);
        }catch (Exception e){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e22"));
        }

        //清除角色对应权限缓存
        return BaseResponse.SUCCESS;
    }

    @Override
    public List<BallCountry> findByCode(String areaCode) {
        QueryWrapper query = new QueryWrapper();
        query.eq("area_code",areaCode);
        return list(query);
    }

    @Override
    public SearchResponse<BallCountry> search(BallCountry role, Integer pageNo, Integer pageSize) {
        SearchResponse<BallCountry> response = new SearchResponse<>();

        Page<BallCountry> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallCountry> query = new QueryWrapper<>();

        IPage<BallCountry> pages = ballCountryMapper.selectPage(page, query);

        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

}
