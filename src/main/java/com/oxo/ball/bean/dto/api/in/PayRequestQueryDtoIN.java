package com.oxo.ball.bean.dto.api.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
/**
 *  发起支付地址:
 *  查询支付地址:
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayRequestQueryDtoIN {
    /**
     * 商户号,配置
     */
    @JsonProperty("merchant_no")
    private String merchantNo;
    /**
     * 平台订单号,配置
     */
    @JsonProperty("trade_no")
    private String tradeNo;
    /**
     * 附加参数
     */
    private Long timestamp;
    /**
     * 签名
     */
    @JsonProperty("sign")
    private String sign;
}
