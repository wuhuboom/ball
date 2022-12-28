package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallCommissionRecharge;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 * 反佣策略 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallCommissionRechargeService extends IService<BallCommissionRecharge> {
    SearchResponse<BallCommissionRecharge> search(BallCommissionRecharge query, Integer pageNo, Integer pageSize);
    BallCommissionRecharge insert(BallCommissionRecharge commissionRecharge) throws ParseException;
    BallCommissionRecharge findOne();
    Boolean delete(BallCommissionRecharge commissionRecharge);
    BaseResponse edit(BallCommissionRecharge commissionRecharge);
    Boolean status(BallCommissionRecharge commissionRecharge);

}
