package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class VirtualCurrencyEditRequest {
    /**
     * 地址
     */
    @NotEmpty(message = "addrNotEmpty")
    private Long id;
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
