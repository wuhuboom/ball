package com.oxo.ball.bean.dto.api.in3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.bean.dto.api.in.PayCallBackDataDtoIN;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PayCallBackDto3 {
    private Integer status;
    private String msg;
    private String code;
    private PayCallBackDto3Data data;
}