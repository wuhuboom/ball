package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallCommissionRecharge;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallCommissionRechargeMapper;
import com.oxo.ball.service.admin.IBallCommissionRechargeService;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 反佣策略 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallCommissionRechargeServiceImpl extends ServiceImpl<BallCommissionRechargeMapper, BallCommissionRecharge> implements IBallCommissionRechargeService {
    @Override
    public SearchResponse<BallCommissionRecharge> search(BallCommissionRecharge queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallCommissionRecharge> response = new SearchResponse<>();
        Page<BallCommissionRecharge> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallCommissionRecharge> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getName())){
            query.eq("name",queryParam.getName());
        }
        if(queryParam.getAutomaticDistribution()!=null){
            query.eq("automatic_distribution",queryParam.getAutomaticDistribution());
        }

        IPage<BallCommissionRecharge> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallCommissionRecharge insert(BallCommissionRecharge commissionRecharge) {
        commissionRecharge.setCreatedAt(System.currentTimeMillis());
        boolean save = save(commissionRecharge);
        return commissionRecharge;
    }

    @Override
    @Cacheable(value = "ball_commission_recharge",key = "'one'",unless = "#result==null")
    public BallCommissionRecharge findOne() {
        BallCommissionRecharge one = getOne(new QueryWrapper<>());
        if(one==null){
            insert(BallCommissionRecharge.builder()
                    .name("充值返佣")
                    .automaticDistribution(0)
                    .autoSettleFirst(0)
                    .status(1)
                    .commissionLevel(1)
                    .build());
            one = getOne(new QueryWrapper<>());
        }
        return one;
    }


    @Override
    @CacheEvict(value = "ball_commission_recharge",key = "#commissionRecharge.getCommissionStrategyType()")
    public Boolean delete(BallCommissionRecharge commissionRecharge) {
        return removeById(commissionRecharge.getId());
    }

    @Override
    @CacheEvict(value = "ball_commission_recharge",key = "'one'")
    public BaseResponse edit(BallCommissionRecharge commissionRecharge) {
        try {
            commissionRecharge.setUpdatedAt(System.currentTimeMillis());
            Collections.sort(commissionRecharge.getOdds(), (o1, o2) -> (int) (o1.getMin()-o2.getMin()));
            Set<String> set = new HashSet<>();
            for(RechargeRebateDto item:commissionRecharge.getOdds()){
                set.add(item.getMin()+""+item.getMax());
            }
            if(commissionRecharge.getOdds().size()!=set.size()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e21"));
            }
            commissionRecharge.setRules(JsonUtil.toJson(commissionRecharge.getOdds()));
            updateById(commissionRecharge);
        }catch (Exception ex){
            return BaseResponse.failedWithMsg("system error");
        }
        return BaseResponse.SUCCESS;
    }

    @Override
    public Boolean status(BallCommissionRecharge slideshow) {
        BallCommissionRecharge edit = BallCommissionRecharge.builder()
                .status(slideshow.getStatus())
                .build();
        edit.setId(slideshow.getId());
        return updateById(edit);
    }
}
