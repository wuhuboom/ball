package com.oxo.ball.bean.dto.api.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayCallBackDtoIN {
    private Integer code;
    private String message;
    private PayCallBackDataDtoIN data;
}
