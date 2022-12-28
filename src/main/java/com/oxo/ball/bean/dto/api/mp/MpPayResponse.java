package com.oxo.ball.bean.dto.api.mp;

import lombok.Data;

@Data
public class MpPayResponse {
    private String code;//	响应状态	String	Y	SUCCESS：成功 FAIL:失败
    private String message;//	响应信息	String	Y	信息说明
    private String pay_url;//	支付链接	String	N	支付链接地址
    private String orderId;//	平台订单号	String	N	平台订单号

}
