package com.oxo.ball.bean.dto.api.in3;

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
public class PayRequestDto3 {
    /**
     * 订单号,系统
     */
    private String orderNo;
    /**
     * 金额,元
     */
    private String orderAmount;
}
