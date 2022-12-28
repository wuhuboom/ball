package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PlayerPhoneChangePwdRequest {
    @NotBlank(message = "usernameIsEmpty")
    private String username;
    @NotEmpty(message = "phoneIsEmpty")
    private String phone;
    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;
    @NotEmpty(message = "newpwdIsEmpty")
    @Size(min = 6, max = 16, message = "passwordSizeError")
    private String newPwd;
    @NotEmpty(message = "confirmedIsEmpty")
    @Size(min = 6, max = 16, message = "passwordSizeError")
    private String twicePwd;
}
