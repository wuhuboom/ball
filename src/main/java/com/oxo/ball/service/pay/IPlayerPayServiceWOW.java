package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.cha.PayRequestDtoCHA;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayServiceWOW {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDtoCHA payRequest);
    BaseResponse payCallBack(PayNoticeDtoCHA payCallBack);
}
