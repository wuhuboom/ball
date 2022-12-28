package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class WithdrawalRequest {
    /**
     * 1今日 2昨日 3.7日 4.10日 5.30日"),
     */
    @NotBlank(message = "withdrawalMoneyNotEmpty")
    @Pattern(regexp = "\\d+(\\.\\d+)?",message = "withdrawalMoneyValid")
    private String money;
    /**
     *
     *1.银行卡,2.USDT,3.SIM"),
     */
    @Min(value = 1,message = "typeError")
    @Max(value = 3,message = "typeError")
    private Integer type;
    @NotEmpty(message = "payPwdNotEmpty")
    private String payPwd;

    /**
     * usdtId
     */
    private Long usdtId;

    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;

    /**
     * 登录地区
     */
    private String ipAddr;
}
