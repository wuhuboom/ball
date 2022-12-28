package com.oxo.ball.bean.dto.api.cha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.bean.dto.api.in.PayCallBackDataDtoIN;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayCallBackDtoCHA {
    public static final String CHA_SUCCESS="SUCCESS";
    public static final String CHA_FAIL="FAIL";
    private String respCode;
    private String tradeMsg;

    private String signType;
    private String sign;
    private String mchId;
    /**
     * 订单号
     */
    private String mchOrderNo;
    /**
     * 实际金额
     */
    private String oriAmount;
    /**
     * 订单金额
     */
    private String tradeAmount;
    private String orderDate;
    /**
     * 平台订单号
     */
    private String orderNo;
    /**
     * 下单状态 1成功
     */
    private String tradeResult;
    /**
     * 付款链接
     */
    private String payInfo;

}
