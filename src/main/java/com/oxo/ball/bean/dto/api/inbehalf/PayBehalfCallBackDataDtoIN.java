package com.oxo.ball.bean.dto.api.inbehalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
/**
 * {
 *   "code": 0, //0为成功，其他数字表示异常，异常描述在message字段中
 *   "data": {
 *     "merchant_no": "6035002", //商户号
 *     "order_amount": "25100", //下单金额，单位分
 *     "order_no": "ZF1655990009416", //商户系统订单号
 *     "sign": "6a8506d7e8387642ba166ef6181cb8a9", //签名
 *     "trade_no": "P16559914233497888", //平台交易单号
 *   }, //返回数据
 *   "message": "success" //描述信息
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBehalfCallBackDataDtoIN {
    @JsonProperty("merchant_no")
    private String merchantNo;
    @JsonProperty("order_amount")
    private String orderAmount;
    @JsonProperty("order_no")
    private String orderNo;
    @JsonProperty("sign")
    private String sign;
    @JsonProperty("trade_no")
    private String tradeNo;
}



