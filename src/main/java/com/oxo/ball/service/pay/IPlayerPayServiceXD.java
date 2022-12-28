package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.web.WebPayCallback;
import com.oxo.ball.bean.dto.api.xd.XdPayCallBack;

public interface IPlayerPayServiceXD {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payParam);
    String payCallBack(XdPayCallBack payCallBackDto);

}
