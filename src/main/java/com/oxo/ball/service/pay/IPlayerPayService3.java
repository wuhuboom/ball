package com.oxo.ball.service.pay;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.api.in3.PayNoticeDto3;
import com.oxo.ball.bean.dto.api.in3.PayRequestDto3;
import com.oxo.ball.bean.dto.resp.BaseResponse;

public interface IPlayerPayService3 {
    String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDto3 payRequest);
    BaseResponse payCallBack(PayNoticeDto3 payCallBack);

}
