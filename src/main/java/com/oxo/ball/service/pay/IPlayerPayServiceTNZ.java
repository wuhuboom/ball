package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamDtoTnz;

public interface IPlayerPayServiceTNZ {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoTnz payParam);
    String payCallBack(PayCallBackDtoTnz payCallBackDto);

}
