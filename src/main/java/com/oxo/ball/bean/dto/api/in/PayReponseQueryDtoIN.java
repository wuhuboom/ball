package com.oxo.ball.bean.dto.api.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayReponseQueryDtoIN {
    private Integer code;
    private String message;
    private PayReponseQueryDataDtoIn data;
}
