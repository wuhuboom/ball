package com.oxo.ball.bean.dto.api.fast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayCallBackDtoFast {
    private String merchantNo;
    private String orderNo;
    private String amount;
    private String realAmount;
    private String ext;
    private String status;
    private String msg;
    private String version;
    private String startTime;
    private String finishPayTime;
    private String platformOrderNo;
    private String replacementOrderNo;
    private String sign;
}
