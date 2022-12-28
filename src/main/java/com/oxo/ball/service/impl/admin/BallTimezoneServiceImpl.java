package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallTimezone;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.mapper.BallTimezoneMapper;
import com.oxo.ball.service.admin.IBallTimezoneService;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.TimeZone;

/**
 * @author flooming
 */
@Service
public class BallTimezoneServiceImpl extends ServiceImpl<BallTimezoneMapper, BallTimezone> implements IBallTimezoneService {
    @Resource
    BallTimezoneMapper ballTimezoneMapper;
    @Resource
    RedisUtil redisUtil;

    @Override
    public BallTimezone findById(Long id) {
        return ballTimezoneMapper.selectById(id);
    }

    @Override
    public BallTimezone findByStatusOn() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        BallTimezone one = getOne(query);
        if(one==null){
            return BallTimezone.builder()
                    .timeId(TimeZone.getDefault().getID())
                    .build();
        }
        return one;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse insert(BallTimezone ballGroup) {
        TimeZone timeZone = TimeZone.getTimeZone(ballGroup.getTimeId());
        if(timeZone==null||!ballGroup.getTimeId().equals(timeZone.getID())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e45"));
        }
        ballGroup.setStatus(0);
        try {
            ballTimezoneMapper.insert(ballGroup);
        }catch (Exception e){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e46"));
        }
        return BaseResponse.SUCCESS;
    }

    @Override
    public Boolean delete(Long id) {
        boolean suc = (ballTimezoneMapper.deleteById(id) == 1);
        return suc;
    }

    @Override
    public BaseResponse edit(BallTimezone editBallTimezone) {
        TimeZone timeZone = TimeZone.getTimeZone(editBallTimezone.getTimeId());
        if(timeZone==null||!editBallTimezone.getTimeId().equals(timeZone.getID())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e45"));
        }
        //清除原来的授权
        try {
            ballTimezoneMapper.updateById(editBallTimezone);
        }catch (Exception e){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e46"));
        }

        //清除角色对应权限缓存
        return BaseResponse.SUCCESS;
    }

    @Override
    public Boolean statusOn(Long id) {
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.set("status",0);
        update(updateWrapper);
        ballTimezoneMapper.updateById(BallTimezone.builder()
                .id(id)
                .status(1)
                .build());
        BallTimezone byId = findById(id);
        TimeUtil.TIME_ZONE = TimeZone.getTimeZone(byId.getTimeId());
        return true;
    }

    @Override
    public SearchResponse<BallTimezone> search(BallTimezone role, Integer pageNo, Integer pageSize) {
        SearchResponse<BallTimezone> response = new SearchResponse<>();

        Page<BallTimezone> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallTimezone> query = new QueryWrapper<>();

        IPage<BallTimezone> pages = ballTimezoneMapper.selectPage(page, query);

        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

}
