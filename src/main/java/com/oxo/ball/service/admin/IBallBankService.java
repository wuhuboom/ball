package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallBank;
import com.oxo.ball.bean.dao.BallBankArea;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.resp.BaseResponse;

import java.util.List;

/**
 * <p>
 * 银行卡 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallBankService extends IService<BallBank> {
    BaseResponse searchBank(BallBank query, Integer pageNo, Integer pageSize);
    List<BallBank> findAll(Integer code, List<BallBankArea> areaId);
    List<BallBank> findAll();
    List<BallBank> findAll(BallPlayer player);
    BaseResponse insert(BallBank ballBank);
    BaseResponse edit(BallBank ballBank);
    BaseResponse del(long id);
    BallBank findById(Long bankId);
    List<BallBank> findByName(String bankName);

    BaseResponse search(BallBankArea ballBankArea,Integer pageNo,Integer pageSize);
    BaseResponse insertArea(BallBankArea ballBankArea);
    BaseResponse editArea(BallBankArea ballBankArea);
    BaseResponse delArea(long id);
    BaseResponse statusArea(BallBankArea id);
    BallBankArea findByAreaId(Long id);
    List<BallBankArea> findEnabled();

    List<BallBankArea> findByCode(Integer payType);

    List<BallBank> findByName(Long areaId, String bankName);
}
