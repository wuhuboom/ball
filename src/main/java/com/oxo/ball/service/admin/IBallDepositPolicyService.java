package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallDepositPolicy;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 存款策略 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallDepositPolicyService extends IService<BallDepositPolicy> {
    SearchResponse<BallDepositPolicy> search(BallDepositPolicy query, Integer pageNo, Integer pageSize);
    BallDepositPolicy getCurrentDepositPolicy(int type);
    BaseResponse insert(BallDepositPolicy depositPolicy);
    Boolean delete(Long id);
    BaseResponse edit(BallDepositPolicy depositPolicy);
    Boolean status(BallDepositPolicy depositPolicy);

    List<RechargeRebateDto> findDiscount(long money, boolean isFirst, boolean isSecond, BallPaymentManagement paymentManagement);
}
