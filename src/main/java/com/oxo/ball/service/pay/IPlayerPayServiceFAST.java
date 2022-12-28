package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.PayCallBackDto;
import com.oxo.ball.bean.dto.api.PayParamDto;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayServiceFAST {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payParam);
    String payCallBack(PayCallBackDtoFast payCallBackDto);

}
