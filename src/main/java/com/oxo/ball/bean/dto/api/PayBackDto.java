package com.oxo.ball.bean.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBackDto {
    //	是	string	平台订单号
    @JsonProperty("PlatformOrder")
    private String platformOrder;
    //	是	string	充值类型(USDT/TRX)
    @JsonProperty("RechargeType")
    private String rechargeType;
    //	是	float	充值金额
    @JsonProperty("AccountOrders")
    private Float accountOrders;
    //实际到账金额
    @JsonProperty("AccountPractical")
    private Float accountPractical;
    //	是	string	回调地址
    @JsonProperty("BackUrl")
    private String backUrl;
}
