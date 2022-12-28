package com.oxo.ball.bean.dto.api.in3;

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
public class PayNoticeDto3 {
    /**
     * 商户号,配置
     */
    private String merchantNo;
    /**
     * 订单号,系统
     */
    private String outTradeNo;
    /**
     * 平台交易号
     */
    private String sn;
    /**
     * 金额,分
     */
    private String amount;
    /**
     * 支付状态 0未支付 1已支付
     */
    private Integer status;
    /**
     * 时间戳,秒
     */
    private String createTime;
    /**
     * 附加参数
     */
    private String extra;
    /**
     * 签名
     */
    private String sign;
}
