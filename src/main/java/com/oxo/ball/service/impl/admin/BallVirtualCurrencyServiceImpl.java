package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallVirtualCurrency;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyDelRequest;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyEditRequest;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallVirtualCurrencyMapper;
import com.oxo.ball.service.admin.IBallVirtualCurrencyService;
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
public class BallVirtualCurrencyServiceImpl extends ServiceImpl<BallVirtualCurrencyMapper, BallVirtualCurrency> implements IBallVirtualCurrencyService {


    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    SomeConfig someConfig;
    @Override
    public List<BallVirtualCurrency> findByPlayerId(Long playerId) {
        QueryWrapper<BallVirtualCurrency> query = new QueryWrapper<>();
        query.eq("player_id",playerId);
        List<BallVirtualCurrency> list = list(query);
        SearchResponse<BallWithdrawManagement> search = withdrawManagementService.search(BallWithdrawManagement.builder()
                .type(2)
                .status(1)
                .build(), 1, 1);
        List<BallWithdrawManagement> results = search.getResults();
        try {
            BallWithdrawManagement ballWithdrawManagement = results.get(0);
            for(BallVirtualCurrency item:list){
                item.setImg(ballWithdrawManagement.getIamgeUrl());
            }
        }catch (Exception ex){}
        return list;
    }

    @Override
    public BaseResponse insert(BallPlayer currentUser,VirtualCurrencyRequest virtualCurrencyRequest) {
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
        try {
            BallVirtualCurrency insert = BallVirtualCurrency.builder()
                    .addr(virtualCurrencyRequest.getAddr())
                    .playerId(currentUser.getId())
                    .statusCheck(0)
                    .protocol("TRC20")
                    .userId(currentUser.getUserId())
                    .username(currentUser.getUsername())
                    .build();
            insert.setCreatedAt(System.currentTimeMillis());
            if(save(insert)){
//                redisUtil.del(smsKey);
                return BaseResponse.successWithMsg("ok");
            }
        }catch (Exception ex){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("addr", "addrExists"));
        }

        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "saveFailed"));
    }

    @Override
    public BaseResponse edit(BallPlayer currPlayer, VirtualCurrencyEditRequest virtualCurrency) {
        //验证支付密码
        if(!currPlayer.getPayPassword().equals(PasswordUtil.genPasswordMd5(virtualCurrency.getPayPwd()))){
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
            if(!o.equals(virtualCurrency.getCode())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallVirtualCurrency edit = BallVirtualCurrency.builder()
                .addr(virtualCurrency.getAddr())
                .build();
        edit.setId(virtualCurrency.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        int editRes = edit(edit);
        if(editRes==0){
            return BaseResponse.SUCCESS;
        }else if(editRes==1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }else{
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "virtualCurrencyEditError"));
        }
    }

    @Override
    public BallVirtualCurrency findById(Long id) {
        return getById(id);
    }

    @Override
    public BaseResponse del(BallPlayer player, VirtualCurrencyDelRequest request) {
//        Object o = redisUtil.get(RedisKeyContant.PLAYER_PHONE_CODE + player.getId());
//        if(o==null){
//            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
//                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
//        }
//        if(!o.equals(request.getCode())){
//            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
//                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
//        }
        BallVirtualCurrency virtualCurrency = findById(request.getId());
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
    public SearchResponse<BallVirtualCurrency> search(BallVirtualCurrency queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallVirtualCurrency> response = new SearchResponse<>();
        Page<BallVirtualCurrency> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallVirtualCurrency> query = new QueryWrapper<>();
        if(queryParam.getStatusCheck()!=null){
            query.eq("status_check",queryParam.getStatusCheck());
        }
        if(!StringUtils.isBlank(queryParam.getUsername())){
            query.eq("username",queryParam.getUsername());
        }
        if(!StringUtils.isBlank(queryParam.getAddr())){
            query.eq("addr",queryParam.getAddr());
        }
        if(!StringUtils.isBlank(queryParam.getProtocol())){
            query.eq("protocol",queryParam.getProtocol());
        }
        if(queryParam.getUserId()!=null){
            query.eq("user_id",queryParam.getUserId());
        }

        IPage<BallVirtualCurrency> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public int edit(BallVirtualCurrency ballBankCard) {
        BallVirtualCurrency edit = BallVirtualCurrency.builder()
                .operUser(ballBankCard.getOperUser())
                .addr(ballBankCard.getAddr())
                .protocol(ballBankCard.getProtocol())
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
    public Boolean status(BallVirtualCurrency ballBankCard) {
        BallVirtualCurrency edit = BallVirtualCurrency.builder()
                .operUser(ballBankCard.getOperUser())
                .status(ballBankCard.getStatus()==1?2:1)
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        return updateById(edit);
    }

    @Override
    public Boolean check(BallVirtualCurrency ballBankCard) {
        BallVirtualCurrency edit = BallVirtualCurrency.builder()
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
