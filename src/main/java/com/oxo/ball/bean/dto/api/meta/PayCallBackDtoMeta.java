package com.oxo.ball.bean.dto.api.meta;

import lombok.Data;

@Data
public class PayCallBackDtoMeta {
    private String orderId;
    private String amount;
    private String amount2;
//    1支付中 2支付成功
    private String status;
    private String extInfo;
}
