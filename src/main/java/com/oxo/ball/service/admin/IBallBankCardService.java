package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallBankCard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

/**
 * <p>
 * 银行卡 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallBankCardService extends IService<BallBankCard> {
    SearchResponse<BallBankCard> search(BallBankCard query, Integer pageNo, Integer pageSize);

    BallBankCard findByPlayerId(Long id);
    BallBankCard findById(Long id);
    boolean insert(BallBankCard save);

    BaseResponse edit(BallBankCard ballBankCard);
    Boolean editById(BallBankCard ballBankCard);

    Boolean status(BallBankCard ballBankCard);

    Boolean check(BallBankCard ballBankCard);

    BallBankCard findByCardNo(String cardNumber);

    void withdrawalSuccess(Long id);

    void unbind(BallBankCard hashCard);

    Boolean delete(Long id);
}
