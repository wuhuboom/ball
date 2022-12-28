package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BindBankCardRequest {

    /**
     * 银行ID
     */
    @NotNull
    private Long bankId;
    @Size(max=100,message = "countryTooLong")
    private String country;
    /**
     * 省份
     */
    @Size(max=100,message = "provinceTooLong")
    private String province;

    /**
     * 城市
     */
    @Size(max=100,message = "cityTooLong")
    private String city;

    /**
     * 支行
     */
    @Size(max=100,message = "subBranchTooLong")
    private String subBranch;
    /**
     * 持卡人姓名
     */
    @NotEmpty(message = "cardNameIsEmpty")
    @Size(max=100,message = "cardNameTooLong")
    private String cardName;
    /**
     * 卡号
     */
    @NotEmpty(message = "cardNumberIsEmpty")
    @Size(max=100,message = "cardNumberTooLong")
    private String cardNumber;

    @NotEmpty(message = "cardNumberTwiceIsEmpty")
    @Size(max=100,message = "cardNumberTwiceTooLong")
    private String cardNumberTwice;
    /**
     * 银行编码
     */
    @NotBlank(message = "bankCodeIsEmpty")
    @Size(max=100,message = "backCodeTooLong")
    private String backCode;

    @NotEmpty(message = "payPwdIsEmpty")
    @Size(max=30,message = "payPwdTooLong")
    private String payPwd;

    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;

    private String identityCard;
    private String phone;
}
