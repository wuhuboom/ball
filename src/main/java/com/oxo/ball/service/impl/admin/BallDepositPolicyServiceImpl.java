package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallDepositPolicy;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallDepositPolicyMapper;
import com.oxo.ball.service.admin.IBallDepositPolicyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static com.oxo.ball.scheduled.JobScheduled.weeks;

/**
 * <p>
 * 存款策略 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallDepositPolicyServiceImpl extends ServiceImpl<BallDepositPolicyMapper, BallDepositPolicy> implements IBallDepositPolicyService {
    @Override
    public SearchResponse<BallDepositPolicy> search(BallDepositPolicy queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallDepositPolicy> response = new SearchResponse<>();
        Page<BallDepositPolicy> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallDepositPolicy> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getDepositPolicyType()!=null){
            query.eq("deposit_policy_type",queryParam.getDepositPolicyType());
        }
        if(queryParam.getPayId()!=null){
            query.eq("pay_id",queryParam.getPayId());
        }
        IPage<BallDepositPolicy> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallDepositPolicy getCurrentDepositPolicy(int type) {
        QueryWrapper<BallDepositPolicy> query = new QueryWrapper<>();
        query.eq("status",1);
        query.eq("deposit_policy_type",type);
        //当前可用的,小于结束时间
        query.le("end_time",TimeUtil.getNowTimeMill());
        //大于开始时间
        query.gt("start_time",TimeUtil.getNowTimeMill());
        query.orderByDesc("preferential_per");
        List<BallDepositPolicy> list = list(query);
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            return list.get(0);
        }
        return null;
    }

    @Override
    public BaseResponse insert(BallDepositPolicy depositPolicy) {
        try {
            if(!StringUtils.isBlank(depositPolicy.getStart())){
                depositPolicy.setStartTime(TimeUtil.stringToTimeStamp(depositPolicy.getStart(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS));
            }
            if(!StringUtils.isBlank(depositPolicy.getEnd())){
                depositPolicy.setEndTime(TimeUtil.stringToTimeStamp(depositPolicy.getEnd(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS));
            }
            depositPolicy.setStatus(1);
            Collections.sort(depositPolicy.getOdds(), (o1, o2) -> (int) (o1.getMin()-o2.getMin()));
            Set<String> set = new HashSet<>();
            for(RechargeRebateDto item:depositPolicy.getOdds()){
                set.add(item.getMin()+""+item.getMax());
            }
            if(depositPolicy.getOdds().size()!=set.size()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e21"));
            }
            depositPolicy.setRules(JsonUtil.toJson(depositPolicy.getOdds()));
            boolean save = save(depositPolicy);
            if(save){
                return BaseResponse.SUCCESS;
            }
        }catch (ParseException e) {
            e.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e21"));
        } catch (IOException e) {
            e.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e23"));
        }catch (Exception ex){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e24"));
        }
        return BaseResponse.failedWithMsg("failed");
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public BaseResponse edit(BallDepositPolicy depositPolicy) {
        try {
            if(!StringUtils.isBlank(depositPolicy.getStart())){
                depositPolicy.setStartTime(TimeUtil.stringToTimeStamp(depositPolicy.getStart(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS));
            }
            if(!StringUtils.isBlank(depositPolicy.getEnd())){
                depositPolicy.setEndTime(TimeUtil.stringToTimeStamp(depositPolicy.getEnd(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS));
            }
            Collections.sort(depositPolicy.getOdds(), (o1, o2) -> (int) (o1.getMin()-o2.getMin()));
            Set<String> set = new HashSet<>();
            for(RechargeRebateDto item:depositPolicy.getOdds()){
                set.add(item.getMin()+""+item.getMax());
            }
            if(depositPolicy.getOdds().size()!=set.size()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e21"));
            }
            depositPolicy.setRules(JsonUtil.toJson(depositPolicy.getOdds()));
            boolean b = updateById(depositPolicy);
            if(b){
                return BaseResponse.SUCCESS;
            }
        }catch (ParseException e) {
            e.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e21"));
        } catch (IOException e) {
            e.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e23"));
        }catch (Exception ex){
            ex.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e24"));
        }
        return BaseResponse.failedWithMsg("update failed");
    }

    @Override
    public Boolean status(BallDepositPolicy slideshow) {
        BallDepositPolicy edit = BallDepositPolicy.builder()
                .status(slideshow.getStatus())
                .build();
        edit.setId(slideshow.getId());
        return updateById(edit);
    }

    @Override
    public List<RechargeRebateDto> findDiscount(long money, boolean isFirst, boolean isSecond, BallPaymentManagement paymentManagement) {
        //1.每种奖励都可以满足条件取优惠
        money /= 100;
        QueryWrapper<BallDepositPolicy> query = new QueryWrapper();
        query.eq("status",1);
        query.eq("pay_id", paymentManagement.getId());
        List<BallDepositPolicy> list = list(query);
        if(list.isEmpty()){
            //USDT 一定会有充值通道
            if(paymentManagement.getPayType()==1){
                return new ArrayList<>();
            }
            query = new QueryWrapper();
            query.eq("status",1);
            query.eq("country",paymentManagement.getCountry());
            list = list(query);
        }
        Map<Integer,List<RechargeRebateDto>> rebateMap = new HashMap<>();
        for(BallDepositPolicy item:list){
            if(item.getPayId()!=null&&paymentManagement.getPayType()!=1){
                continue;
            }
            //不是首充,过滤
            if(!isFirst&&(item.getDepositPolicyType()==1||item.getDepositPolicyType()==0)){
                continue;
            }
//            boolean inTimeBegin = false;
//            boolean inTimeEnd = false;
//            //不在时间区间,过滤
//            if(item.getStartTime()==0||curr>item.getStartTime()){
//                inTimeBegin = true;
//            }
//            if(item.getEndTime()==0||curr<item.getEndTime()){
//                inTimeEnd = true;
//            }
//            if(!inTimeBegin||!inTimeEnd){
//                continue;
//            }
            int nowWeek = weeks[TimeUtil.getNowWeek()-1];
            if(item.getWeek()!=nowWeek&&item.getDepositPolicyType()==4){
                //不在固定日
                continue;
            }
            //不是次充,过滤
            if(!isSecond&&item.getDepositPolicyType()==3){
                continue;
            }
            try {
                List<RechargeRebateDto> rechargeRebateDtos = JsonUtil.fromJsonToList(item.getRules(), RechargeRebateDto.class);
                for(RechargeRebateDto item1:rechargeRebateDtos){
                    item1.setId(item.getId());
                    item1.setStartTime(item.getStartTime());
                    item1.setEndTime(item.getEndTime());
                    item1.setDepositPolicyType(item.getDepositPolicyType());
                    item1.setPayId(item.getPayId());
                    item1.setAutoSettlement(item.getAutoSettlement()==1?true:false);
                }
                List<RechargeRebateDto> rechargeRebateDtos1 = rebateMap.get(item.getDepositPolicyType());
                if(rechargeRebateDtos1==null){
                    rechargeRebateDtos1 = new ArrayList<>();
                    rebateMap.put(item.getDepositPolicyType(),rechargeRebateDtos1);
                }
                rechargeRebateDtos1.addAll(rechargeRebateDtos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(rebateMap.isEmpty()){
            return new ArrayList<>();
        }
        List<RechargeRebateDto> rechargeRebateDtos = new ArrayList<>();
        for(List<RechargeRebateDto> item : rebateMap.values()){
            Collections.sort(item, (o1, o2) -> (int) (o2.getMax()-o1.getMax()));
            for(RechargeRebateDto sitem:item){
                if(money>=sitem.getMin()&&money<=sitem.getMax()){
                    //判定是否在区间,因为是倒序,所以满足条件则为命中
                    rechargeRebateDtos.add(sitem);
                    break;
                }
            }
        }
        return rechargeRebateDtos;
    }

    private Long countBonus(Long realMoney, Integer rate, Long fixed){
        long bonus = 0L;
        if(rate!=null&&rate>0){
            double div = BigDecimalUtil.div(rate, 100);
            Double disc = BigDecimalUtil.mul(realMoney, div);
            bonus =  disc.longValue();
        }
        if(fixed!=null&&fixed>0){
            bonus+=(fixed*BigDecimalUtil.PLAYER_MONEY_UNIT);
        }
        return bonus;
    }
}
