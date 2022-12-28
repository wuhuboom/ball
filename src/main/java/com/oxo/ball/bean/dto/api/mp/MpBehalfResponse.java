package com.oxo.ball.bean.dto.api.mp;

import lombok.Data;

@Data
public class MpBehalfResponse {
    private String code;//	响应状态	String	Y	SUCCESS：成功 FAIL:失败
    private String message;//	响应信息	String	Y	信息说明
    private String mer_no;//	商户号	String	Y	商户编号
    private String settle_id;//	商户代付订单号	String	Y	商户申请代付的订单号
    private String sign;//	签名	String	Y	MD5签名

}
