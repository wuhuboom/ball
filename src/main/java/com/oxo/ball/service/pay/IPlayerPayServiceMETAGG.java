package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.meta.PayCallBackDtoMeta;
import com.oxo.ball.bean.dto.api.tnz.PayParamDtoTnz;

public interface IPlayerPayServiceMETAGG {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoTnz payParam);
    String payCallBack(PayCallBackDtoMeta payCallBackDto);

}
