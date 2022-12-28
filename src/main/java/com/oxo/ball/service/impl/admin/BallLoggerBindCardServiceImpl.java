package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallBankCard;
import com.oxo.ball.bean.dao.BallLoggerBindCard;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallLoggerBindCardMapper;
import com.oxo.ball.service.admin.IBallLoggerBindCardService;
import com.oxo.ball.service.admin.IBallWithdrawManagementService;
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
public class BallLoggerBindCardServiceImpl extends ServiceImpl<BallLoggerBindCardMapper, BallLoggerBindCard> implements IBallLoggerBindCardService {

    @Autowired
    IBallWithdrawManagementService withdrawManagementService;

    @Override
    public SearchResponse<BallLoggerBindCard> search(BallBankCard queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerBindCard> response = new SearchResponse<>();
        Page<BallLoggerBindCard> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerBindCard> query = new QueryWrapper<>();
        if(queryParam.getPlayerId()!=null){
            query.eq("player_id",queryParam.getPlayerId());
        }
        if(!StringUtils.isBlank(queryParam.getCardNumber())){
            query.eq("card_number",queryParam.getCardNumber());
        }
        query.orderByDesc("created_at");
        IPage<BallLoggerBindCard> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public boolean insert(BallLoggerBindCard save) {
        return save(save);
    }

    @Override
    public BallLoggerBindCard findByCardNumber(String cardNumber, Long id) {
        QueryWrapper query = new QueryWrapper();
        query.eq("card_number",cardNumber);
        query.ne("player_id",id);
        List<BallLoggerBindCard> list = list(query);
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            return list.get(0);
        }
        return null;
    }
}
