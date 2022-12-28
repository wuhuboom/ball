package com.oxo.ball.bean.dto.api.inbehalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBehalfNoticeDtoIN {
    /**
     * 商户号,配置
     */
    private String merchant_no;
    /**
     * 订单号,系统
     */
    private String order_no;
    /**
     * 平台交易号
     */
    private String trade_no;
    /**
     * 金额,分
     */
    private Integer order_amount;
    /**
     * 订单实际金额,分
     */
    private Integer pay_amount;
    /**
     * 0下单成功,1处理中,2代付成功,4代付失败
     */
    private Integer pay_status;
    /**
     * 时间戳,秒
     */
    private Long timestamp;
    /**
     * 附加参数
     */
    private String attach;
    /**
     * 签名
     */
    private String sign;

    /**
     * 取消原因
     */
    private String cancel_message;
}
