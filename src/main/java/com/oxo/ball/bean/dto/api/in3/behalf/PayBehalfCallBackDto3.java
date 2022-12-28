package com.oxo.ball.bean.dto.api.in3.behalf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.bean.dto.api.in3.PayCallBackDto3Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class PayBehalfCallBackDto3 {
    private Integer status;
    private String msg;
    private String code;
    private PayBehalfCallBackDto3Data data;
}