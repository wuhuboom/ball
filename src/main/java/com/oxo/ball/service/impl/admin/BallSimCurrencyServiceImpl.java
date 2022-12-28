package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSimCurrency;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.oxo.ball.bean.dto.req.player.SimCurrencyDelRequest;
import com.oxo.ball.bean.dto.req.player.SimCurrencyRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallSimCurrencyMapper;
import com.oxo.ball.service.admin.IBallSimCurrencyService;
import com.oxo.ball.service.admin.IBallWithdrawManagementService;
import com.oxo.ball.utils.PasswordUtil;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 银行卡 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallSimCurrencyServiceImpl extends ServiceImpl<BallSimCurrencyMapper, BallSimCurrency> implements IBallSimCurrencyService {


    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    SomeConfig someConfig;
    @Override
    public BallSimCurrency findByPlayerId(Long playerId) {
        QueryWrapper<BallSimCurrency> query = new QueryWrapper<>();
        query.eq("player_id",playerId);
        BallSimCurrency list = getOne(query);
        SearchResponse<BallWithdrawManagement> search = withdrawManagementService.search(BallWithdrawManagement.builder()
                .type(3)
                .status(1)
                .build(), 1, 1);
        List<BallWithdrawManagement> results = search.getResults();
        try {
            BallWithdrawManagement ballWithdrawManagement = results.get(0);
            list.setImg(ballWithdrawManagement.getIamgeUrl());
        }catch (Exception ex){}
        return list;
    }

    @Override
    public BaseResponse insert(BallPlayer currentUser,SimCurrencyRequest virtualCurrencyRequest) {
        if(StringUtils.isBlank(currentUser.getPhone())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        if(!currentUser.getPayPassword().equals(PasswordUtil.genPasswordMd5(virtualCurrencyRequest.getPayPwd()))){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "payPwdError"));
        }
        String smsKey = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
        Object o = redisUtil.get(smsKey);
        if(someConfig.getApiSwitch()==null){
            if(o==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if(!o.equals(virtualCurrencyRequest.getCode())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
//        String key = RedisKeyContant.PLAYER_SIM_LIMIT + currentUser.getId();
//        Object o1 = redisUtil.get(key);
//        if(o1!=null){
//            return BaseResponse.SUCCESS ;
//        }
//        long incr = redisUtil.incr(key, 1);
//        redisUtil.expire(key,2);
//        if(incr>1){
//            return BaseResponse.SUCCESS ;
//        }

        BallSimCurrency byPlayerId = findByPlayerId(currentUser.getId());
        if(byPlayerId!=null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "simAlreadyBind"));
        }

        try {
            BallSimCurrency insert = BallSimCurrency.builder()
                    .simName(virtualCurrencyRequest.getName())
                    .sim(virtualCurrencyRequest.getSim())
                    .playerId(currentUser.getId())
                    .statusCheck(0)
                    .username(currentUser.getUsername())
                    .build();
            insert.setCreatedAt(System.currentTimeMillis());
            if(save(insert)){
//                redisUtil.del(smsKey);
                return BaseResponse.successWithMsg("ok");
            }
        }catch (Exception ex){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("addr", "simExists"));
        }

        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "saveFailed"));
    }

    @Override
    public BaseResponse edit(BallPlayer currPlayer, SimCurrencyRequest simCurrencyRequest) {
        //验证支付密码
        if(!currPlayer.getPayPassword().equals(PasswordUtil.genPasswordMd5(simCurrencyRequest.getPayPwd()))){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "payPwdError"));
        }
        //验证短信验证码
        if(someConfig.getApiSwitch()==null){
            String smsKey =RedisKeyContant.PLAYER_PHONE_CODE + currPlayer.getId();
//            redisUtil.del(smsKey);
            Object o = redisUtil.get(smsKey);
            if(o==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if(!o.equals(simCurrencyRequest.getCode())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallSimCurrency byPlayerId = findByPlayerId(currPlayer.getId());
        if(byPlayerId==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "simNotExists"));
        }
        BallSimCurrency edit = BallSimCurrency.builder()
                .simName(simCurrencyRequest.getName())
                .sim(simCurrencyRequest.getSim())
                .build();
        edit.setId(byPlayerId.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        int editRes = edit(edit);
        if(editRes==0){
            return BaseResponse.SUCCESS;
        }else if(editRes==1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "simExists"));
        }else{
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "simCurrencyEditError"));
        }
    }

    @Override
    public BallSimCurrency findById(Long id) {
        return getById(id);
    }

    @Override
    public BaseResponse del(BallPlayer player, SimCurrencyDelRequest request) {
//        Object o = redisUtil.get(RedisKeyContant.PLAYER_PHONE_CODE + player.getId());
//        if(o==null){
//            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
//                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
//        }
//        if(!o.equals(request.getCode())){
//            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
//                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
//        }
        BallSimCurrency virtualCurrency = findById(request.getId());
        if(virtualCurrency!=null&&virtualCurrency.getPlayerId().equals(player.getId())){
            boolean b = super.removeById(request.getId());
            if(b){
                return BaseResponse.successWithMsg("ok");
            }
        }else{
            BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "delFailed"));
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "delFailed"));
    }

    @Override
    public SearchResponse<BallSimCurrency> search(BallSimCurrency queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallSimCurrency> response = new SearchResponse<>();
        Page<BallSimCurrency> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallSimCurrency> query = new QueryWrapper<>();
        if(queryParam.getStatusCheck()!=null){
            query.eq("status_check",queryParam.getStatusCheck());
        }
        if(!StringUtils.isBlank(queryParam.getUsername())){
            query.eq("username",queryParam.getUsername());
        }
        if(!StringUtils.isBlank(queryParam.getSim())){
            query.eq("sim",queryParam.getSim());
        }
        if(!StringUtils.isBlank(queryParam.getSimName())){
            query.eq("sim_name",queryParam.getSimName());
        }

        IPage<BallSimCurrency> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public int edit(BallSimCurrency ballBankCard) {
        BallSimCurrency edit = BallSimCurrency.builder()
                .operUser(ballBankCard.getOperUser())
                .sim(ballBankCard.getSim())
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        try {
            updateById(ballBankCard);
        }catch (DuplicateKeyException e){
            return 1;
        }
        return 0;
    }

    @Override
    public Boolean status(BallSimCurrency ballBankCard) {
        BallSimCurrency edit = BallSimCurrency.builder()
                .operUser(ballBankCard.getOperUser())
                .status(ballBankCard.getStatus()==1?2:1)
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        return updateById(edit);
    }

    @Override
    public Boolean check(BallSimCurrency ballBankCard) {
        BallSimCurrency edit = BallSimCurrency.builder()
                .operUser(ballBankCard.getOperUser())
                .statusCheck(ballBankCard.getStatusCheck())
                .checker(ballBankCard.getOperUser())
                .checkTime(System.currentTimeMillis())
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        return updateById(edit);
    }
}
