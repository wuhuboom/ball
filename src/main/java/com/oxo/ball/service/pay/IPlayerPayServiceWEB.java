package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.web.WebPayCallback;

public interface IPlayerPayServiceWEB {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payParam);
    String payCallBack(WebPayCallback payCallBackDto);

}
