package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallBankCard;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallBankCardMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallBankCardService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.admin.IBallWithdrawManagementService;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BallBankCardServiceImpl extends ServiceImpl<BallBankCardMapper, BallBankCard> implements IBallBankCardService {

    @Autowired
    IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    IBasePlayerService basePlayerService;

    @Override
    public SearchResponse<BallBankCard> search(BallBankCard queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBankCard> response = new SearchResponse<>();
        Page<BallBankCard> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBankCard> query = new QueryWrapper<>();
        if(queryParam.getStatusCheck()!=null){
            query.eq("status_check",queryParam.getStatusCheck());
        }
        if(!StringUtils.isBlank(queryParam.getUsername())){
            query.eq("username",queryParam.getUsername());
        }
        if(!StringUtils.isBlank(queryParam.getCardNumber())){
            query.eq("card_number",queryParam.getCardNumber());
        }
        if(!StringUtils.isBlank(queryParam.getCardName())){
            query.eq("card_name",queryParam.getCardName());
        }
        if(queryParam.getUserId()!=null){
            query.eq("user_id",queryParam.getUserId());
        }
        if(queryParam.getHasWithdrawal()!=null){
            query.eq("has_withdrawal",queryParam.getHasWithdrawal());
        }

        query.orderByDesc("id");
        IPage<BallBankCard> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallBankCard findByPlayerId(Long id) {
        QueryWrapper<BallBankCard> query = new QueryWrapper<>();
        query.eq("player_id",id);
        BallBankCard one = getOne(query);
        if(one!=null){
            SearchResponse<BallWithdrawManagement> search = withdrawManagementService.search(BallWithdrawManagement.builder()
                    .type(1)
                    .status(1)
                    .build(), 1, 1);
            List<BallWithdrawManagement> results = search.getResults();
            for(BallWithdrawManagement item:results){
                one.setImg(item.getIamgeUrl());
            }
            return one;
        }
        return null;
    }

    @Override
    public BallBankCard findById(Long id) {
        return getById(id);
    }

    @Override
    public boolean insert(BallBankCard save) {
        return save(save);
    }

    @Override
    public BaseResponse edit(BallBankCard ballBankCard) {
        BallBankCard edit = BallBankCard.builder()
                .operUser(ballBankCard.getOperUser())
                .cardName(ballBankCard.getCardName())
                .cardNumber(ballBankCard.getCardNumber())
                .country(ballBankCard.getCountry())
                .city(ballBankCard.getCity())
                .bankName(ballBankCard.getBankName())
                .backEncoding(ballBankCard.getBackEncoding())
                .subBranch(ballBankCard.getSubBranch())
                .province(ballBankCard.getProvince())
                .playerId(ballBankCard.getPlayerId())
                .username(ballBankCard.getUsername())
                .userId(ballBankCard.getUserId())
                .identityCard(ballBankCard.getIdentityCard())
                .phone(ballBankCard.getPhone())
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        BallBankCard byId = findById(ballBankCard.getId());
        if(!ballBankCard.getUsername().equals(byId.getUsername())){
            //换绑
            BallPlayer player = basePlayerService.findByUsername(ballBankCard.getUsername());
            if(player==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e15"));
            }
            //换绑账号是否绑定了银行卡
            BallBankCard byPlayerId = findByPlayerId(player.getId());
            if(byPlayerId!=null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e16"));
            }
            edit.setPlayerId(player.getId());
            edit.setUsername(player.getUsername());
            edit.setUserId(player.getUserId());
        }
        boolean b = updateById(edit);
        return b?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("fail");
    }

    @Override
    public Boolean editById(BallBankCard ballBankCard) {
        boolean b = updateById(ballBankCard);
        return b;
    }

    @Override
    public Boolean status(BallBankCard ballBankCard) {
        BallBankCard edit = BallBankCard.builder()
                .operUser(ballBankCard.getOperUser())
                .status(ballBankCard.getStatus()==1?2:1)
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        return updateById(edit);
    }

    @Override
    public Boolean check(BallBankCard ballBankCard) {
        BallBankCard edit = BallBankCard.builder()
                .operUser(ballBankCard.getOperUser())
                .statusCheck(ballBankCard.getStatus())
                .checker(ballBankCard.getOperUser())
                .checkTime(System.currentTimeMillis())
                .build();
        edit.setId(ballBankCard.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        return updateById(edit);
    }

    @Override
    public BallBankCard findByCardNo(String cardNumber) {
        QueryWrapper query = new QueryWrapper();
        query.eq("card_number",cardNumber);
        List<BallBankCard> list = list(query);
        if(list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public void withdrawalSuccess(Long id) {
        BallBankCard edit = BallBankCard.builder()
                .hasWithdrawal(1)
                .build();
        edit.setId(id);
        edit.setUpdatedAt(System.currentTimeMillis());
        updateById(edit);
    }

    @Override
    public void unbind(BallBankCard hashCard) {
        if(hashCard==null){
            return;
        }
        UpdateWrapper update = new UpdateWrapper();
        update.eq("id",hashCard.getId());
        update.set("player_id",null);
        update.set("username","");
        update.set("user_id",null);
        update.set("updated_at",System.currentTimeMillis());
        update(update);
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }
}
