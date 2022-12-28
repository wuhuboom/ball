package com.oxo.ball.bean.dto.api.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayNoticeDtoIN {
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
     * 支付代码
     */
//    private Integer payment_type;
    /**
     * 支付状态 0未支付 1已支付
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
}
