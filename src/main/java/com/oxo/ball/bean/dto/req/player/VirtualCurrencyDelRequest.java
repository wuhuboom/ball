package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VirtualCurrencyDelRequest {
    /**
     * id
     */
    @NotEmpty(message = "virtualCurrencyNotEmpty")
    private Long id;

//    @NotEmpty(message = "smsCodeIsEmpty")
//    private String code;
}
