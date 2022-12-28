package com.oxo.ball.bean.dto.api.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayReponseQueryDataDtoIn {
    /**
     * 商户号,配置
     */
    @JsonProperty("merchant_no")
    private String merchantNo;
    /**
     * 订单号,系统
     */
    @JsonProperty("order_no")
    private String orderNo;
    /**
     * 平台交易号
     */
    @JsonProperty("trade_no")
    private String tradeNo;
    /**
     * 金额,分
     */
    @JsonProperty("order_amount")
    private Integer orderAmount;
    /**
     * 订单实际金额,分
     */
    @JsonProperty("pay_amount")
    private Integer payamount;
    @JsonProperty("pay_status")
    private Integer payStatus;
    /**
     * 签名
     */
    @JsonProperty("sign")
    private String sign;
}
