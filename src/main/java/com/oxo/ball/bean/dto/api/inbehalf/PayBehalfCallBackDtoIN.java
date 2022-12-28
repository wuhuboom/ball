package com.oxo.ball.bean.dto.api.inbehalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBehalfCallBackDtoIN {
    private Integer code;
    private String message;
    private PayBehalfCallBackDataDtoIN data;
}
