package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.api.mp.MpPayCallBack;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayServiceMP {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payRequest);
    String payCallBack(MpPayCallBack payCallBack);

}
