package com.oxo.ball.bean.dto.api.cha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/**
 *  发起支付地址:
 *  查询支付地址:
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayRequestDtoCHA {
    /**
     * 订单号,系统
     */
    @JsonProperty("order_no")
    private String orderNo;
    /**
     * 金额,分
     */
    @JsonProperty("order_amount")
    private String orderAmount;

    private String phone;
}
