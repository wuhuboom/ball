package com.oxo.ball.bean.dto.api.inbehalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/**
 *  发起代付地址:
 *  查询代付地址:
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBehalfRequestDtoIN {
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
     * 金额,分
     */
    @JsonProperty("order_amount")
    private Integer orderAmount;
    /**
     * 通知地址
     */
    @JsonProperty("noticeUrl")
    private String noticeUrl;

    /**
     * 银行编码
     */
    @JsonProperty("bank_code")
    private String bankCode;
    /**
     * 姓名
     */
    @JsonProperty("account_name")
    private String accountName;
    /**
     * 卡号
     */
    @JsonProperty("account_no")
    private String accountNo;

    /**
     * 时间戳,秒
     */
    private Long timestamp;
    /**
     * 支付成功跳转
     */
    @JsonProperty("return_url")
    private String returnUrl;
    /**
     * 附加参数
     */
    private String attach;
    /**
     * 签名
     */
    @JsonProperty("sign")
    private String sign;
}
