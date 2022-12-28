package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class VirtualCurrencyRequest {
    /**
     * 地址
     */
    @NotEmpty(message = "addrNotEmpty")
    private String addr;

    @NotEmpty(message = "payPwdNotEmpty")
    private String payPwd;

    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;
}
