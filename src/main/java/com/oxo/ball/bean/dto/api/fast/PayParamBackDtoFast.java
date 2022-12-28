package com.oxo.ball.bean.dto.api.fast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayParamBackDtoFast {
    private String code;
    private String message;
    private String version;
    private String merchantNo;
    private String orderNo;
    private String platformOrderNo;
    private String url;
    private String amount;
    private String sign;
    private String status;
}
