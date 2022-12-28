package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayServiceIN {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDtoIN payRequest);
    BaseResponse payCallBack(PayNoticeDtoIN payCallBack);

}
