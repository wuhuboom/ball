package com.oxo.ball.bean.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayParamDto {
    public static String PAY_TYPE_USDT = "USDT";
    public static String PAY_TYPE_TRX = "TRX";
    //	是	string	用户名
    @JsonProperty("Username")
    private String username;
    //	是	string	平台订单号
    @JsonProperty("PlatformOrder")
    private String platformOrder;

    //	是	string	充值类型(USDT/TRX)
    @JsonProperty("RechargeType")
    private String rechargeType;
    //	是	float	充值金额
    @JsonProperty("AccountOrders")
    private Float accountOrders;
    //	是	string	回调地址
    @JsonProperty("BackUrl")
    private String backUrl;
}
