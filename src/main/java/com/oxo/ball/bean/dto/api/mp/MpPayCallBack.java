package com.oxo.ball.bean.dto.api.mp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpPayCallBack {
    private String payResult;//	订单状态	String	Y	1：支付成功 其他:失败
    private String mer_no;//	商户号	String	Y	商户编号
    private String orderNo;//	商家订单号	String	Y	商家代收订单号
    private String payAmount;//	交易金额	String	Y	支付金额
    private String ptOrderNo;//	平台订单号	String	Y	平台唯一订单号
    private String currency;//	币种	String	Y	币种
    private String sign;//	签名	String	Y	不参与签名
    private String ext;

}
