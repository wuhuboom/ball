package com.oxo.ball.bean.dto.api.cha;

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
public class PayNoticeDtoCHA {
    /**
     * 状态
     */
    private String tradeResult;
    /**
     *
     */
    private String mchId;
    /**
     * 订单号
     */
    private String mchOrderNo;
    /**
     * 原始金额
     */
    private String oriAmount;
    /**
     * 实际金额
     */
    private String amount;
    /**
     *
     */
    private String orderDate;
    /**
     * 平台订单号
     */
    private String orderNo;
    private String merRetMsg;
    private String signType;
    private String sign;
}
