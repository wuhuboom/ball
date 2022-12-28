package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallBankCard;
import com.oxo.ball.bean.dao.BallLoggerBindCard;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 银行卡 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallLoggerBindCardService extends IService<BallLoggerBindCard> {
    SearchResponse<BallLoggerBindCard> search(BallBankCard query, Integer pageNo, Integer pageSize);
    boolean insert(BallLoggerBindCard save);

    BallLoggerBindCard findByCardNumber(String cardNumber, Long id);
}
