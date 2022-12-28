package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.PayCallBackDto;
import com.oxo.ball.bean.dto.api.PayParamDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayService {
    String requestPayUrl(BallPaymentManagement paymentManagement,PayParamDto payParam);
    BaseResponse payCallBack(BallPaymentManagement paymentManagement,PayCallBackDto payCallBackDto);

}
