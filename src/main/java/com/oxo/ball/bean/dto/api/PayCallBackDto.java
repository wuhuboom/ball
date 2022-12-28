package com.oxo.ball.bean.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayCallBackDto {
    //{"Code":200,"Msg":"success","Result":{"Data"
    @JsonProperty("Code")
    private Integer code;
    @JsonProperty("Msg")
    private String msg;
    @JsonProperty("Result")
    private Object result;
}
