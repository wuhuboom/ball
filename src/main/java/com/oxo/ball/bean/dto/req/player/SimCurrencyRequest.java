package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SimCurrencyRequest {
    @NotEmpty(message = "vsimIsEmpty")
    private String sim;

    @NotEmpty(message = "vsimNameIsEmpty")
    private String name;

    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;

    @NotEmpty(message = "payPwdNotEmpty")
    private String payPwd;


}
