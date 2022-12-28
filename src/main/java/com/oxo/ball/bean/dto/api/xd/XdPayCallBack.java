package com.oxo.ball.bean.dto.api.xd;

import lombok.Data;

@Data
public class XdPayCallBack {
    private String platOrderId;//	string	平台订单号
    private String orderId;//	string	商户订单号
    private Double amount;//	number	实际支付金额
    private Integer status;//	int	交易状态，值见数据字典
    private String sign	;//string	签名
    private String ext;//
}
