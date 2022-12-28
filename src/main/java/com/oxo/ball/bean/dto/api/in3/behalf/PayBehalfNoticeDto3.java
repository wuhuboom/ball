package com.oxo.ball.bean.dto.api.in3.behalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PayBehalfNoticeDto3 {
    private String merchantNo;
    private String outTradeNo;
    private String type;
    private String amount;
    private Integer status;
    private String createTime;
    private String extra;
    private String sign;
    private String sn;
}
